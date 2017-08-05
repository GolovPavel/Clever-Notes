package ru.pavel.noteproject;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.pavel.noteproject.dao.Database;

/**
 * Created by pavel on 22.07.17.
 */
public class NoteServer {

    private static Server jettyServer;

    public static void startServer() throws Exception {
        Database.setUp();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[]{
                createResourceContext(),
                createServletContext()

        });
        int port = Integer.parseInt(System.getenv("PORT"));
        System.out.println("The port is " + port);
        jettyServer = new Server(port);
        jettyServer.setHandler(contexts);

        jettyServer.start();
    }


    private static ContextHandler createResourceContext() {
        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        ResourceHandler handler = new ResourceHandler();
        handler.setWelcomeFiles(new String[]{"index.html"});

        String serverRoot = NoteServer.class.getResource("/bootstrap").toString();
        handler.setResourceBase(serverRoot);
        context.setHandler(handler);
        return context;
    }

    private static ServletContextHandler createServletContext() {
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/service/");
        ServletHolder jerseyServlet = context.addServlet(
                org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter(
                "jersey.config.server.provider.packages",
                "ru.pavel.noteproject"
        );

        jerseyServlet.setInitParameter(
                "com.sun.jersey.spi.container.ContainerResponseFilters",
                CrossBrowserFilter.class.getCanonicalName()
        );

        return context;
    }

    public static void stopServer() throws Exception {
        jettyServer.stop();
    }

    public static void main(String[] args) throws Exception {
        startServer();
    }

}
