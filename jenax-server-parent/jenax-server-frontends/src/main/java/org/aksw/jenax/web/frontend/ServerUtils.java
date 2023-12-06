package org.aksw.jenax.web.frontend;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.Objects;

import org.eclipse.jetty.ee9.annotations.AnnotationConfiguration;
import org.eclipse.jetty.ee9.nested.ContextHandler.APIContext;
import org.eclipse.jetty.ee9.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.ee9.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.ee9.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee9.webapp.WebAppContext;
import org.eclipse.jetty.ee9.webapp.WebInfConfiguration;
import org.eclipse.jetty.ee9.webapp.WebXmlConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.WebApplicationInitializer;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

/**
 * Utils to start a jetty server.
 *
 * http://stackoverflow.com/questions/10738816/deploying-a-servlet-
 * programmatically-with-jetty
 * http://stackoverflow.com/questions/3718221/add-resources
 * -to-jetty-programmatically
 *
 * @author raven
 *
 *         http://kielczewski.eu/2013/11/using-embedded-jetty-spring-mvc/
 */
public class ServerUtils {

    private static final Logger logger = LoggerFactory.getLogger(ServerUtils.class);

    public static Server startServer(int port, WebApplicationInitializer initializer) {
        Server result = startServer(ServerUtils.class, port, initializer);
        return result;
    }

    public static Server startServer(Class<?> clazz, int port, WebApplicationInitializer initializer) {
        String externalForm = getExternalForm(clazz);
        Server result = startServer(port, externalForm, initializer);
        return result;
    }

    public static Server startServer(int port, String externalForm, WebApplicationInitializer initializer) {
        Server server = prepareServer(port, externalForm, initializer);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return server;
    }

    public static Server prepareServer(int port, WebApplicationInitializer initializer) {
        // Not sure if using this class always works as expected
        Server result = prepareServer(ServerUtils.class, port, initializer);
        return result;
    }

    public static String getExternalForm(Class<?> clazz) {
        ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        String externalForm = location.toExternalForm();

        if (logger.isDebugEnabled()) {
            logger.debug("Trying to resolve webapp by starting from location (external form): " + externalForm);
        }

        Path path;
        try {
            // TODO This assumes that builds are done under /target/classes/
            path = Paths.get(location.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // Try to detect whether we are being run from an
        // archive (uber jar / war) or just from compiled classes
        if (externalForm.endsWith("/classes/")) {
            Path webappFolder = path.resolve("../../src/main/webapp").normalize();
            if(Files.exists(webappFolder)) {
                externalForm = webappFolder.toString();
            }
        } else if(externalForm.endsWith("-classes.jar")) {
            Path parent = path.getParent();
            String rawFilename = "" + path.getFileName();
            String filename = rawFilename.replace("-classes.jar", ".war");
            // Try if replacing '-classes.jar' with '.war' also exists
            Path warPath = parent.resolve(filename);
            if(Files.exists(warPath)) {
                externalForm = warPath.toString();
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Resolved webapp location to: " + externalForm);
        }

        return externalForm;
    }

    public static Server prepareServer(Class<?> clazz, int port, WebApplicationInitializer initializer) {
        String externalForm = getExternalForm(clazz);
        if (logger.isDebugEnabled()) {
            logger.debug("Loading webAppContext from " + externalForm);
        }
        Server result = prepareServer(port, externalForm, initializer);
        return result;
    }

    public static Server prepareServer(int port, String externalForm, WebApplicationInitializer initializer) {
        Server server = new Server(port);
        WebAppContext webAppContext = new WebAppContext();

//        Configurations.setKnown(
//                "org.eclipse.jetty.ee9.webapp.JettyWebXmlConfiguration",
//                "org.eclipse.jetty.ee9.annotations.AnnotationConfiguration");

        ServletContext servletContext = webAppContext.getServletContext();
        // ee9:
        APIContext api = (APIContext)servletContext;

        // ee10:
        // ServletContextApi api = (ServletContextApi)servletContext;

        // Needed to support spring's ContextLoaderListener
        api.setExtendedListenerTypes(true);

        webAppContext.addConfiguration(
            new AnnotationConfiguration(),
            new WebXmlConfiguration(),
            new PlusConfiguration(),
            new JettyWebXmlConfiguration(),
            new WebInfConfiguration(),
            new MetaInfConfiguration()
        );

        // If we are running not from a war but a src/main/webapp folder,
        // register the listener programmatically
        if (externalForm == null || !externalForm.endsWith(".war")) {
            Objects.requireNonNull(initializer, "Configuration from non-war file requires an WebAppInitializer");
            webAppContext.addEventListener(new LifeCycle.Listener() {
                @Override
                public void lifeCycleStarting(LifeCycle arg0) {
                    try {
                        initializer.onStartup(servletContext);
                    } catch (ServletException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        webAppContext.setServer(server);
        webAppContext.setContextPath("/");
        // context.setDescriptor(externalForm + "/WEB-INF/web.xml");
        webAppContext.setWar(externalForm);
        server.setHandler(webAppContext);
        return server;
    }
}

//webAppContext.setInitParameter("org.apache.tomcat.InstanceManager", "org.apache.tomcat.SimpleInstanceManager");
    // server.setHandler(getServletContextHandler(getContext()));

    // SocketConnector connector = new SocketConnector();
    //
    // // Set some timeout options to make debugging easier.
    // connector.setMaxIdleTime(1000 * 60 * 60);
    // connector.setSoLingerTime(-1);
    // connector.setPort(port);
    // server.setConnectors(new Connector[] { connector });

//webAppContext.setInitParameter("org.apache.tomcat.InstanceManager", "org.apache.tomcat.SimpleInstanceManager");

    // AnnotationConfigWebApplicationContext rootContext = new
    // AnnotationConfigWebApplicationContext();
    // rootContext.register(AppConfig.class);
    //
    // // Manage the lifecycle of the root application context
    // webAppContext.addEventListener(new
    // ContextLoaderListener(rootContext));
    // webAppContext.addEventListener(new RequestContextListener());

    // webAppContext.addEventListener(new ContextLoaderListener(context);
    // Context servletContext = webAppContext.getServletContext();

    // These lines are required to get JSP working with jetty
    // https://github.com/puppetlabs/trapperkeeper-webserver-jetty9/issues/140

    // Configurations.setKnown(null);

//    Configuration.ClassList classlist = Configuration.ClassList
//            .setServerDefault( server );
//    classlist.addBefore(
//            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
//            "org.eclipse.jetty.annotations.AnnotationConfiguration" );

    // public void mainGrizzly() {
    // HttpServer server = new HttpServer();
    //
    // final NetworkListener listener = new NetworkListener("grizzly",
    // NetworkListener.DEFAULT_NETWORK_HOST, PACS.RESTPort);
    // server.addListener(listener);
    //
    // ResourceConfig rc = new ResourceConfig();
    // rc.packages("org.aksw.facete2.web");
    // HttpHandler processor =
    // ContainerFactory.createContainer(GrizzlyHttpContainer.class, rc);
    // server.getServerConfiguration().addHttpHandler(processor, "");
    // }
