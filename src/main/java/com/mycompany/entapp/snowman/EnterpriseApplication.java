/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman;

// J21-TODO: Build currently targets Java 17 in CI. Upgrade to Java 21 when environment supports it.

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;

/**
 * PUBLIC_INTERFACE
 * Entry point for the Snowman application when running the shaded executable JAR.
 * This class is referenced by the maven-shade-plugin Manifest mainClass to enable
 * `java -jar target/Snowman.jar`.
 */
public class EnterpriseApplication {

    private static final int DEFAULT_PORT = 3001;

    private EnterpriseApplication() {
    }

    public static void main(String[] args) throws Exception {

        final Server server = new Server();

        final ServerConnector serverConnector = new ServerConnector(server);

        final int port = resolvePort();
        serverConnector.setPort(port);

        // Simple startup log to confirm resolved port precedence at runtime
        System.out.println("[Snowman] Starting embedded Jetty on port " + port
                + " (precedence: -Dserver.port > -Dport > PORT env > default 3001)");

        server.setConnectors(new Connector[]{serverConnector});

        WebAppContext webAppContext = new WebAppContext();
        // Set descriptor and base resource using Jetty Resource API for compatibility
        webAppContext.setDescriptor(getResourceFilePath("webapp/WEB-INF/web.xml"));
        Resource base = Resource.newResource(getResourceFilePath("webapp"));
        webAppContext.setBaseResource(base);
        webAppContext.setContextPath("/");
        webAppContext.setParentLoaderPriority(true);

        // JSP handling note:
        // This application does not use JSPs. We intentionally do NOT register a JSP servlet
        // or JspHandler to avoid unnecessary initialization and warnings on startup.

        server.setHandler(webAppContext);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (server.isStarted()) {
                    server.setStopAtShutdown(true);

                    try {
                        server.stop();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }));

        server.join();

    }

    private static String getResource(String resourceName) {
        URL resourceURL = EnterpriseApplication.class.getClassLoader().getResource(resourceName);
        if (resourceURL == null) {
            throw new RuntimeException("Unable to fetch specified resource: " + resourceName);
        }
        return resourceURL.toString();
    }

    private static String getResourceFilePath(String resourceName) {
        URL resourceURL = EnterpriseApplication.class.getClassLoader().getResource(resourceName);
        if (resourceURL == null) {
            throw new RuntimeException("Unable to fetch specified resource: " + resourceName);
        }
        // URL#getFile provides a decoded filesystem path suitable for Jetty resource resolution
        return resourceURL.getFile();
    }

    // PUBLIC_INTERFACE
    /**
     * Resolves the HTTP server port from multiple inputs with the following precedence:
     * 1) -Dserver.port system property (Spring/standard convention)
     * 2) -Dport system property (legacy/custom)
     * 3) PORT environment variable (common in PaaS)
     * 4) Default port (3001)
     *
     * @return resolved port number to bind the Jetty server to.
     */
    private static int resolvePort() {
        // Try standard server.port first
        String serverPortProp = System.getProperty("server.port");
        if (serverPortProp != null && !serverPortProp.isEmpty()) {
            try {
                return Integer.parseInt(serverPortProp.trim());
            } catch (NumberFormatException ignored) {
                // fallthrough to next option
            }
        }
        // Legacy/custom -Dport
        String legacyPortProp = System.getProperty("port");
        if (legacyPortProp != null && !legacyPortProp.isEmpty()) {
            try {
                return Integer.parseInt(legacyPortProp.trim());
            } catch (NumberFormatException ignored) {
                // fallthrough to next option
            }
        }
        // Environment variable PORT
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isEmpty()) {
            try {
                return Integer.parseInt(envPort.trim());
            } catch (NumberFormatException ignored) {
                // ignore and use default
            }
        }
        return DEFAULT_PORT;
    }
}
