/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * EnterpriseApplication boots an embedded Jetty as a REST-only service using Spring MVC.
 * No WAR-style webapp folder is required; the Spring DispatcherServlet is configured programmatically.
 *
 * Notes:
 * - Jetty dependencies are aligned to pom property ${jetty.version} (9.4.53.v20231009).
 * - maven-shade-plugin is configured to avoid minimizing away required Jetty classes.
 * - server.port defaults to 3001 and can be overridden with -Dport or -Dserver.port.
 */
public class EnterpriseApplication {

    // PUBLIC_INTERFACE
    /**
     * Application entrypoint. Bootstraps an embedded Jetty server and registers Spring's DispatcherServlet
     * to serve REST controllers discovered by the Spring context defined under META-INF/application-context.xml.
     *
     * System properties:
     * -Dport=<int>        Optional. The TCP port that Jetty should listen on. Defaults to 3001 if not provided.
     * -Dserver.port=<int> Optional. Alternative property name supported by the platform.
     *
     * To run:
     *   java -jar target/Snowman.jar
     * or with a custom port:
     *   java -Dserver.port=3001 -jar target/Snowman.jar
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

        // Create a context handler for servlets without relying on a WAR/webapp folder
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        // Initialize Spring root context via ContextLoaderListener
        XmlWebApplicationContext rootContext = new XmlWebApplicationContext();
        rootContext.setConfigLocation("classpath:META-INF/application-context.xml");
        context.addEventListener(new ContextLoaderListener(rootContext));

        // Register Spring DispatcherServlet mapped to "/"
        XmlWebApplicationContext mvcContext = new XmlWebApplicationContext();
        mvcContext.setParent(rootContext);
        // Reuse prior servlet XML if any additional beans are needed; otherwise, controller component scan picks up @RestController
        mvcContext.setConfigLocation("classpath:/webapp/WEB-INF/SpringServlet-servlet.xml");
        DispatcherServlet dispatcherServlet = new DispatcherServlet(mvcContext);
        ServletHolder servletHolder = new ServletHolder("SpringServlet", dispatcherServlet);
        servletHolder.setInitOrder(1);
        context.addServlet(servletHolder, "/");

        server.setHandler(context);
        server.start();

        // Clean shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.setStopAtShutdown(true);
            try {
                if (server.isStarted()) {
                    server.stop();
                }
            } catch (Exception e) {
                // swallow to allow JVM exit
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
