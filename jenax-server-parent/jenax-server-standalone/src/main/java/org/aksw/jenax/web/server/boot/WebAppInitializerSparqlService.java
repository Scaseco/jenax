package org.aksw.jenax.web.server.boot;

import org.aksw.jenax.web.frontend.WebAppInitializerSparqlServiceUtils;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class WebAppInitializerSparqlService
    implements  WebApplicationInitializer
{
    protected WebApplicationContext rootContext;

    public WebAppInitializerSparqlService(WebApplicationContext rootContext) {
        this.rootContext = rootContext;
    }

    @Override
    public void onStartup(ServletContext servletContext)
            throws ServletException {
        WebAppInitializerSparqlServiceUtils.init(servletContext, rootContext);
    }

//    public static WebApplicationInitializer create(Class<?> config) {
//        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
//        rootContext.register(config);
//
//        WebApplicationInitializer result = new WebAppInitializerSparqlService(rootContext);
//
//        return result;
//    }
}
