/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;

public class EnterpriseApplication {

    // PUBLIC_INTERFACE
    /**
     * Application entrypoint. Bootstraps an embedded Jetty server and deploys the bundled webapp found under
     * classpath:/webapp using web.xml for the servlet configuration.
     *
     * System properties:
     * -Dport=<int>    Optional. The TCP port that Jetty should listen on. Defaults to 8090 if not provided.
     *
     * To run:
     *   java -jar target/Snowman.jar
     * or with a custom port:
     *   java -Dport=3001 -jar target/Snowman.jar
     */

    private static final int DEFAULT_PORT = 8090;

    private EnterpriseApplication() {
    }

    public static void main(String[] args) throws Exception {

        final Server server = new Server();

        final ServerConnector serverConnector = new ServerConnector(server);

        serverConnector.setPort(resolvePort());

        server.setConnectors(new Connector[]{serverConnector});

        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setDescriptor(getResource("webapp/WEB-INF/web.xml"));
        // For compatibility across Jetty versions, use setWar to point to the exploded webapp directory
        webAppContext.setWar(getResource("webapp"));
        webAppContext.setContextPath("/");
        webAppContext.setParentLoaderPriority(true);

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

    // PUBLIC_INTERFACE
    /**
     * Resolve a classpath resource to a filesystem path string for Jetty configuration.
     *
     * @param resourceName the path to the resource relative to the classpath root (e.g., "webapp/WEB-INF/web.xml")
     * @return a filesystem path to the resource
     * @throws RuntimeException if the resource cannot be found
     */
    private static String getResource(String resourceName) {
        URL resourceURL = EnterpriseApplication.class.getClassLoader().getResource(resourceName);
        if (resourceURL == null) {
            throw new RuntimeException("Unable to fetch specified resource: " + resourceName);
        }
        // Jetty WebAppContext#setResourceBase expects a filesystem path; use URL.getFile()
        return resourceURL.getFile();
    }

    private static int resolvePort() {
        // Compatibility: prefer -Dport, but if absent and -Dserver.port is provided (used by preview),
        // use that value as a fallback. Defaults to 8090 if neither is provided or parsing fails.
        String primary = System.getProperty("port");
        String fallback = System.getProperty("server.port");
        String candidate = (primary != null && !primary.isEmpty()) ? primary : fallback;

        if (candidate != null && !candidate.isEmpty()) {
            try {
                return Integer.parseInt(candidate);
            } catch (NumberFormatException ignore) {
                // ignore and use default
            }
        }
        return DEFAULT_PORT;
    }
}
