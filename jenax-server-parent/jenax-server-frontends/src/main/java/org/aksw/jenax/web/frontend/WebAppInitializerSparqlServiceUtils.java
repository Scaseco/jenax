package org.aksw.jenax.web.frontend;

import org.aksw.jenax.web.provider.QueryExceptionProvider;
import org.aksw.jenax.web.provider.UncaughtExceptionProvider;
import org.aksw.jenax.web.provider.UnwrapRuntimeExceptionProvider;
import org.aksw.jenax.web.servlet.ServletSparqlServiceImpl;
import org.aksw.jenax.web.util.WebAppInitUtils;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;


/**
 * Convenience WebAppInitializer for setting up a SPARQL service
 *
 * Note this class no longer inherits from WebApplicationInitializer,
 * because servlets environments may wrongly pick up this class as an entry point
 * to the application.
 *
 */
public class WebAppInitializerSparqlServiceUtils {


    public static void init(ServletContext servletContext, WebApplicationContext rootContext) {

        WebAppInitUtils.defaultSetup(servletContext, rootContext);

        {
            ServletRegistration.Dynamic servlet = servletContext.addServlet("sparqlServiceServlet", new ServletContainer());
            //servlet.setInitParameter("contextConfigLocation", "workaround-for-JERSEY-2038");
            servlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, String.join(",",
                    ServletSparqlServiceImpl.class.getCanonicalName(),
                    QueryExceptionProvider.class.getCanonicalName(),
                    UnwrapRuntimeExceptionProvider.class.getCanonicalName(),
                    UncaughtExceptionProvider.class.getCanonicalName()
            ));
//            servlet.setInitParameter(ServletProperties.FILTER_FORWARD_ON_404, "true");
//            servlet.setInitParameter(ServletProperties.FILTER_STATIC_CONTENT_REGEX, ".*(html|css|js)");
            servlet.addMapping("/sparql/*");
            servlet.setAsyncSupported(true);
            servlet.setLoadOnStartup(1);
        }

        // Dispatcher servlet is used to serve the html/js/css resources
        {
            AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
            dispatcherContext.register(WebMvcConfigSparql.class);

            // Beware! Spring ignores subsequent WebMvcConfigurers
            // So one cannot modularize simply by adding multiple WebMvcConfigurers!
            // dispatcherContext.register(WebMvcConfigSnorql.class);

            ServletRegistration.Dynamic servlet = servletContext.addServlet("dispatcherServlet", new DispatcherServlet(dispatcherContext));
            servlet.addMapping("/*");
            servlet.setAsyncSupported(true);
            servlet.setLoadOnStartup(1);
        }
    }
}
