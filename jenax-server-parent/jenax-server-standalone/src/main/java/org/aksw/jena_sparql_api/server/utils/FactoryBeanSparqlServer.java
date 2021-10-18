package org.aksw.jena_sparql_api.server.utils;

import java.util.function.Function;

import org.aksw.jena_sparql_api.web.server.ServerUtils;
import org.aksw.jenax.arq.datasource.RdfDataSource;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParser;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.web.servlet.RdfConnectionFactory;
import org.apache.jena.query.Syntax;
import org.eclipse.jetty.server.Server;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class FactoryBeanSparqlServer {
    protected Integer port;

    /** Resolver of an arbitrary request to a connection. The returned connection should be a fresh one
     * and it will be closed once the request processing is done */
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

    public FactoryBeanSparqlServer setSparqlServiceFactory(RdfDataSource dataSource) {
        this.setSparqlServiceFactory(request -> dataSource.getConnection());

        return this;
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

        rootContext.getBeanFactory().registerSingleton("sparqlConnectionFactory", connectionFactory);
        rootContext.getBeanFactory().registerSingleton("sparqlStmtParser", sparqlStmtParser);

        Server result = ServerUtils.startServer(port, new WebAppInitializerSparqlService(rootContext));
        return result;
    }

    public static FactoryBeanSparqlServer newInstance() {
        return new FactoryBeanSparqlServer();
    }
}
