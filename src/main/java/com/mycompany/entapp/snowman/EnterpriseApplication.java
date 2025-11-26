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

import java.net.InetAddress;
import java.net.URL;
import java.util.Objects;

/**
 * PUBLIC_INTERFACE
 * Entry point for the Snowman application when running the shaded executable JAR.
 * This class is referenced by the maven-shade-plugin Manifest mainClass to enable
 * `java -jar target/Snowman.jar`.
 *
 * The application starts an embedded Jetty server and blocks the main thread via server.join(),
 * ensuring the process stays alive under the preview system until an external SIGTERM/SIGINT is sent.
 */
public class EnterpriseApplication {

    private static final int DEFAULT_PORT = 3001;
    private static final String DEFAULT_HOST = "0.0.0.0";

    private EnterpriseApplication() {
    }

    public static void main(String[] args) throws Exception {
        // Build server and connector
        final Server server = new Server();
        final ServerConnector serverConnector = new ServerConnector(server);

        final int port = resolvePort();
        final String host = resolveHost();

        serverConnector.setPort(port);
        serverConnector.setHost(host);

        // Simple startup log to confirm resolved address/port precedence at runtime
        System.out.println("[Snowman] Starting embedded Jetty on " + host + ":" + port
                + " (precedence: -Dserver.port > -Dport > PORT env > default 3001; host via -Dserver.address or 0.0.0.0)");

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

        try {
            server.start();
            System.out.println("[Snowman] Jetty started. Listening on http://" + host + ":" + port + "/");
        } catch (Exception startEx) {
            System.err.println("[Snowman] FATAL: Jetty failed to start: " + startEx.getMessage());
            startEx.printStackTrace(System.err);
            // Do not call System.exit; allow the runtime to surface the failure naturally.
            throw startEx;
        }

        // Add a robust shutdown hook that logs a reason and stops server gracefully.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("[Snowman] Shutdown hook triggered. Initiating graceful shutdown...");
                if (server.isStopping() || server.isStopped()) {
                    System.out.println("[Snowman] Server already stopping/stopped.");
                    return;
                }
                server.setStopAtShutdown(true);
                server.stop();
                System.out.println("[Snowman] Jetty stopped successfully.");
            } catch (Exception e) {
                System.err.println("[Snowman] Error during shutdown: " + e.getMessage());
                e.printStackTrace(System.err);
            }
        }, "snowman-shutdown-hook"));

        // Block main thread so process stays alive.
        try {
            server.join();
            System.out.println("[Snowman] server.join() completed; process will exit now.");
        } catch (InterruptedException ie) {
            // Log and restore interrupt status; do not System.exit here.
            System.err.println("[Snowman] Main thread interrupted (likely SIGINT/SIGTERM): " + ie.getMessage());
            Thread.currentThread().interrupt();
        }
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

    // PUBLIC_INTERFACE
    /**
     * Resolves the HTTP server bind address with the following precedence:
     * 1) -Dserver.address system property (Spring convention)
     * 2) Default 0.0.0.0 (bind all interfaces)
     *
     * @return resolved host string to bind Jetty.
     */
    private static String resolveHost() {
        String addr = System.getProperty("server.address");
        if (addr != null && !addr.isEmpty()) {
            return addr.trim();
        }
        return DEFAULT_HOST;
    }
}
