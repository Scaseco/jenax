package org.aksw.jenax.web.server.boot;

import java.util.function.Function;

import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionModular;
import org.aksw.jenax.dataaccess.sparql.connection.query.SparqlQueryConnectionJsaBase;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParser;
import org.aksw.jenax.web.filter.FilterPost;
import org.aksw.jenax.web.filter.SparqlStmtTypeAcceptHeaderFilter;
import org.aksw.jenax.web.frontend.WebMvcConfigSnorql;
import org.aksw.jenax.web.frontend.WebMvcConfigSparql;
import org.aksw.jenax.web.provider.QueryExceptionProvider;
import org.aksw.jenax.web.provider.UncaughtExceptionProvider;
import org.aksw.jenax.web.provider.UnwrapRuntimeExceptionProvider;
import org.aksw.jenax.web.servlet.RdfConnectionFactory;
import org.aksw.jenax.web.servlet.ServletSparqlServiceImpl;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServletRequest;

public class ServletBuilderSparql
    implements ServletBuilder
{
    protected RdfConnectionFactory connectionFactory;
    protected SparqlStmtParser sparqlStmtParser;

    public static ServletBuilderSparql newBuilder() {
        return new ServletBuilderSparql();
    }

    public RdfConnectionFactory getSparqlServiceFactory() {
        return connectionFactory;
    }

    public ServletBuilderSparql setSparqlServiceFactory(RdfConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    public ServletBuilderSparql setSparqlServiceFactory(QueryExecutionFactoryQuery qef) {
        return setSparqlServiceFactory((HttpServletRequest request) -> new RDFConnectionModular(new SparqlQueryConnectionJsaBase<>(qef), null, null));
    }

    public ServletBuilderSparql setSparqlServiceFactory(RdfDataSource dataSource) {
        return setSparqlServiceFactory((HttpServletRequest request) -> dataSource.getConnection());
    }

    public Function<String, SparqlStmt> getSparqlStmtParser() {
        return sparqlStmtParser;
    }

    public ServletBuilderSparql setSparqlStmtParser(SparqlStmtParser sparqlStmtParser) {
        this.sparqlStmtParser = sparqlStmtParser;
        return this;
    }

    @Override
    public WebApplicationInitializer build(GenericWebApplicationContext rootContext) {
        ConfigurableListableBeanFactory beanFactory = rootContext.getBeanFactory();

        beanFactory.registerSingleton("sparqlConnectionFactory", connectionFactory);
        beanFactory.registerSingleton("sparqlStmtParser", sparqlStmtParser);

        WebApplicationInitializer result = servletContext ->
            ServletBuilderSparql.initServlet(servletContext, rootContext);
        return result;
    }

    public static void initServlet(ServletContext servletContext, GenericWebApplicationContext rootContext) {
        String path = "/sparql/*";

        // TODO The filters here are hacky as they fiddle with headers and message body which e.g. breaks consuming the full post body in a servlet

        {
            FilterRegistration.Dynamic fr = servletContext.addFilter("FilterPost", new FilterPost());
            fr.addMappingForUrlPatterns(null, true, path);
            fr.setAsyncSupported(true);
        //  fr.setInitParameter("dispatcher", "REQUEST");
        }

        {
            // The sparqlStmtParser is set in FactoryBeanSparqlServer - getting it out here
            // is not ideal
            GenericWebApplicationContext cxt = (GenericWebApplicationContext)rootContext;
            SparqlStmtParser sparqlStmtParser = (SparqlStmtParser)cxt.getBeanFactory().getSingleton("sparqlStmtParser");

            FilterRegistration.Dynamic fr = servletContext.addFilter("SparqlStmtTypeAcceptHeaderFilter", new SparqlStmtTypeAcceptHeaderFilter(sparqlStmtParser));
            fr.addMappingForUrlPatterns(null, true, path);
            fr.setAsyncSupported(true);
        //  fr.setInitParameter("dispatcher", "REQUEST");
        }

        {
            ServletRegistration.Dynamic servlet = servletContext.addServlet("sparqlServiceServlet", new ServletContainer());
            //servlet.setInitParameter("contextConfigLocation", "workaround-for-JERSEY-2038");
            servlet.setInitParameter(ServerProperties.PROVIDER_CLASSNAMES, String.join(",",
                    ServletSparqlServiceImpl.class.getCanonicalName(),
                    QueryExceptionProvider.class.getCanonicalName(),
                    UnwrapRuntimeExceptionProvider.class.getCanonicalName(),
                    UncaughtExceptionProvider.class.getCanonicalName()
            ));
    //        servlet.setInitParameter(ServletProperties.FILTER_FORWARD_ON_404, "true");
    //        servlet.setInitParameter(ServletProperties.FILTER_STATIC_CONTENT_REGEX, ".*(html|css|js)");
            servlet.addMapping(path);
            servlet.setAsyncSupported(true);
            servlet.setLoadOnStartup(1);
        }

        // Dispatcher servlet is used to serve the html/js/css resources
        // This is hacky as it registers the mapping under "/*" which is not local to the sparql endpoint
        {
            AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
            dispatcherContext.register(WebMvcConfigSparql.class);

            ServletRegistration.Dynamic servlet = servletContext.addServlet("dispatcherServlet", new DispatcherServlet(dispatcherContext));
            servlet.addMapping("/");
            servlet.setAsyncSupported(true);
            servlet.setLoadOnStartup(1);
        }
    }
}
