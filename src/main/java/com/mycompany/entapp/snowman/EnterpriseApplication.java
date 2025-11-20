/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * EnterpriseApplication boots an embedded Jetty and serves the webapp packaged inside the shaded JAR.
 * This configuration uses classpath resources for the webapp so the application runs directly from the
 * shaded JAR without unpacking a WAR directory on disk.
 *
 * Notes:
 * - Jetty dependencies are aligned to pom property ${jetty.version} (9.4.53.v20231009) with compile scope.
 * - maven-shade-plugin does not minimize and merges META-INF/services to keep Jetty modules (e.g. ShutdownThread).
 * - Web resources under src/main/resources/webapp/** are packaged to classpath:/webapp for WebAppContext.
 */
public class EnterpriseApplication {

    // PUBLIC_INTERFACE
    /**
     * Application entrypoint. Bootstraps an embedded Jetty server and deploys the bundled webapp found under
     * classpath:/webapp using web.xml for the servlet configuration.
     *
     * System properties:
     * -Dport=<int>        Optional. The TCP port that Jetty should listen on. Defaults to 3001 if not provided.
     * -Dserver.port=<int> Optional. Alternative property name supported by the platform.
     *
     * To run:
     *   java -jar target/Snowman.jar
     * or with a custom port:
     *   java -Dport=3001 -jar target/Snowman.jar
     */
    private static final int DEFAULT_PORT = 3001;

    private EnterpriseApplication() {
        // no-op
    }

    public static void main(String[] args) throws Exception {
        final Server server = new Server();
        final ServerConnector connector = new ServerConnector(server);
        connector.setPort(resolvePort());
        server.setConnectors(new Connector[]{connector});

        // Configure WebAppContext to load resources from classpath so it works from shaded JAR
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setParentLoaderPriority(true);

        // Use Jetty Resource to load from classpath. Do NOT set WAR file system path.
        // Ensure descriptor (web.xml) and base resource (webapp root) are resolved from classpath.
        Resource webXml = Resource.newClassPathResource("/webapp/WEB-INF/web.xml");
        if (webXml == null || !webXml.exists()) {
            throw new IllegalStateException("Cannot locate web.xml at classpath:/webapp/WEB-INF/web.xml");
        }
        webAppContext.setDescriptor(webXml.getURI().toString());

        Resource webappRoot = Resource.newClassPathResource("/webapp");
        if (webappRoot == null || !webappRoot.exists()) {
            throw new IllegalStateException("Cannot locate webapp directory at classpath:/webapp");
        }
        webAppContext.setBaseResource(webappRoot);

        server.setHandler(webAppContext);
        server.start();

        // Clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.setStopAtShutdown(true);
            try {
                if (server.isStarted()) {
                    server.stop();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }));

        server.join();
    }

    private static int resolvePort() {
        // Compatibility: prefer -Dport, but if absent and -Dserver.port is provided, use that.
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
