package org.aksw.jenax.web.server.boot;

import org.aksw.jenax.graphql.json.api.GraphQlExecFactory;
import org.aksw.jenax.web.provider.QueryExceptionProvider;
import org.aksw.jenax.web.provider.UncaughtExceptionProvider;
import org.aksw.jenax.web.provider.UnwrapRuntimeExceptionProvider;
import org.aksw.jenax.web.servlet.ServletGraphQlEndpointImpl;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.GenericWebApplicationContext;

import jakarta.servlet.ServletRegistration;

public class ServletBuilderGraphQl
    implements ServletBuilder
{
    protected GraphQlExecFactory graphQlExecFactory;

    public static ServletBuilderGraphQl newBuilder() {
        return new ServletBuilderGraphQl();
    }

    public GraphQlExecFactory getGraphQlExecFactory() {
        return graphQlExecFactory;
    }

    public ServletBuilderGraphQl setGraphQlExecFactory(GraphQlExecFactory graphQlExecFactory) {
        this.graphQlExecFactory = graphQlExecFactory;
        return this;
    }

    @Override
    public WebApplicationInitializer build(GenericWebApplicationContext rootContext) {
        ConfigurableListableBeanFactory beanFactory = rootContext.getBeanFactory();

        beanFactory.registerSingleton("graphQlExecFactory", graphQlExecFactory);

        WebApplicationInitializer result = servletContext -> {
            {
                ServletRegistration.Dynamic servlet = servletContext.addServlet("graphqlServiceServlet", new ServletContainer());
                //servlet.setInitParameter("contextConfigLocation", "workaround-for-JERSEY-2038");
                servlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, String.join(",",
                        ServletGraphQlEndpointImpl.class.getCanonicalName(),
                        QueryExceptionProvider.class.getCanonicalName(),
                        UnwrapRuntimeExceptionProvider.class.getCanonicalName(),
                        UncaughtExceptionProvider.class.getCanonicalName()
                ));
        //        servlet.setInitParameter(ServletProperties.FILTER_FORWARD_ON_404, "true");
        //        servlet.setInitParameter(ServletProperties.FILTER_STATIC_CONTENT_REGEX, ".*(html|css|js)");
                servlet.addMapping("/graphql/*");
                servlet.setAsyncSupported(true);
                servlet.setLoadOnStartup(1);
            }

//            // Dispatcher servlet is used to serve the html/js/css resources
//            {
//                AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
////                dispatcherContext.register(WebMvcConfigSnorql.class);
//                dispatcherContext.register(WebMvcConfigYasgui.class);
//
//                ServletRegistration.Dynamic servlet = servletContext.addServlet("dispatcherServlet", new DispatcherServlet(dispatcherContext));
//                servlet.addMapping("/*");
//                servlet.setAsyncSupported(true);
//                servlet.setLoadOnStartup(1);
//            }
        };

        return result;
    }
}
