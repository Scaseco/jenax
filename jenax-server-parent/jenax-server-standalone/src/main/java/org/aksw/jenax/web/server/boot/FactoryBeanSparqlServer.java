package org.aksw.jenax.web.server.boot;

import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionModular;
import org.aksw.jenax.dataaccess.sparql.connection.query.SparqlQueryConnectionJsaBase;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParser;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.web.frontend.ServerUtils;
import org.aksw.jenax.web.servlet.RdfConnectionFactory;
import org.apache.jena.query.Syntax;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Server;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.context.support.GenericWebApplicationContext;

/** Use ServerBuilder instead which is not limited to a single sparql servlet */
@Deprecated
public class FactoryBeanSparqlServer {
    protected Integer port;

    /** Resolver of http servlet requests to connections.
     * Implementations may choose to ignore or validate any request arguments.
     * The returned connection must be a fresh one
     * and it will be closed once the request processing is done. */
    protected RdfConnectionFactory connectionFactory;
    protected SparqlStmtParser sparqlStmtParser;

    public int getPort() {
        return port;
    }

    public FactoryBeanSparqlServer setPort(int port) {
        this.port = port;

        return this;
    }

    public RdfConnectionFactory getSparqlServiceFactory() {
        return connectionFactory;
    }

    public FactoryBeanSparqlServer setSparqlServiceFactory(RdfConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        return this;
    }

    public FactoryBeanSparqlServer setSparqlServiceFactory(QueryExecutionFactoryQuery qef) {
        return setSparqlServiceFactory((HttpServletRequest request) ->
            new RDFConnectionModular(new SparqlQueryConnectionJsaBase<>(qef), null, null));
    }

    public FactoryBeanSparqlServer setSparqlServiceFactory(RdfDataSource dataSource) {
        return setSparqlServiceFactory((HttpServletRequest request) -> dataSource.getConnection());
    }

    public Function<String, SparqlStmt> getSparqlStmtParser() {
        return sparqlStmtParser;
    }

    public FactoryBeanSparqlServer setSparqlStmtParser(SparqlStmtParser sparqlStmtParser) {
        this.sparqlStmtParser = sparqlStmtParser;

        return this;
    }


    public Server create() {
        if (port == null) {
            port = 7531;
        }

        if (sparqlStmtParser == null) {
            sparqlStmtParser = SparqlStmtParserImpl.create(Syntax.syntaxARQ, true);
        }

        if (connectionFactory == null) {
            throw new RuntimeException("SparqlServiceFactory must not be null");
        }

        GenericWebApplicationContext rootContext = new GenericWebApplicationContext();
        ConfigurableListableBeanFactory beanFactory = rootContext.getBeanFactory();

        beanFactory.registerSingleton("sparqlConnectionFactory", connectionFactory);
        beanFactory.registerSingleton("sparqlStmtParser", sparqlStmtParser);

        Server result = ServerUtils.startServer(port, new WebAppInitializerSparqlService(rootContext));

//        WebAppContext webAppContext = (WebAppContext)result.getHandler();
//        webAppContext.getSessionHandler().setMaxInactiveInterval(90 * 24 * 60 * 60);

        for (org.eclipse.jetty.server.Connector connector : result.getConnectors()) {
            if (connector instanceof AbstractConnector) {
                ((AbstractConnector) connector).setIdleTimeout(90 * 24 * 60 * 60);
            }
        }

        return result;
    }

    public static FactoryBeanSparqlServer newInstance() {
        return new FactoryBeanSparqlServer();
    }
}
