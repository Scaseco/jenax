package org.aksw.jenax.dataaccess.sparql.connection.common;

import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactories;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactoryQuery;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkModular;

public class RDFConnections {
    /**
     * Provide a limited {@link RDFConnection} over query {@link QueryExecFactoryQuery}.
     * See {@link RDFConnections#of(QueryExecFactoryQuery)
     */
    public static RDFConnection of(QueryExecutionFactoryQuery qef) {
        QueryExecFactoryQuery qExec = QueryExecFactories.adaptQuery(qef);
        RDFConnection result = of(qExec);
        return result;
    }

    /** Provide a limited {@link RDFConnection} over query {@link QueryExecutionFactoryQuery}.
     * The connection only supports querying (no update or dataset operations)
     * The connection does not support transactions.
     */
    public static RDFConnection of(QueryExecFactoryQuery qef) {
        LinkSparqlQuery queryLink = QueryExecFactories.toLink(qef);
        RDFLink rdfLink = new RDFLinkModular(queryLink, null, null);
        RDFConnection result = RDFConnectionAdapter.adapt(rdfLink);
        return result;
    }
}
