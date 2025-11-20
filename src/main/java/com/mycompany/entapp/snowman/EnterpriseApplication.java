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

public class EnterpriseApplication {

    private static final int DEFAULT_PORT = 8090;

    private EnterpriseApplication() {
    }

    public static void main(String[] args) throws Exception {

        final Server server = new Server();

        final ServerConnector serverConnector = new ServerConnector(server);

        serverConnector.setPort(resolvePort());

        server.setConnectors(new Connector[]{serverConnector});

        WebAppContext webAppContext = new WebAppContext();
        // Set descriptor and base resource using Jetty Resource API for compatibility
        webAppContext.setDescriptor(getResourceFilePath("webapp/WEB-INF/web.xml"));
        Resource base = Resource.newResource(getResourceFilePath("webapp"));
        webAppContext.setBaseResource(base);
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

    private static int resolvePort() {
        try {
            return Integer.parseInt(System.getProperty("port"));
        } catch (NumberFormatException ex) {
            return DEFAULT_PORT;
        }
    }
}
