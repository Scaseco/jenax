package org.aksw.jenax.dataaccess.sparql.exec.query;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryOverQueryExecFactory;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryJenaxBase;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.exec.QueryExecAdapter;

/** Central utility class for working with QueryExecFactory* classes */
public class QueryExecFactories {
    public static QueryExecFactory adapt(QueryExecutionFactory qef) {
        QueryExecFactory result = qef instanceof QueryExecutionFactoryOverQueryExecFactory
                ? ((QueryExecutionFactoryOverQueryExecFactory)qef).getDecoratee()
                : new QueryExecFactoryOverQueryExecutionFactory(qef);

        return result;
    }

    public static QueryExecFactory adapt(QueryExecFactoryQuery qef) {
        QueryExecFactory result = qef instanceof QueryExecFactory
                ? (QueryExecFactory)qef
                : new QueryExecFactoryBackQuery(qef, QueryFactory::create);

        return result;
    }

    /** {@link LinkSparqlQuery} -&gt; {@link QueryExecFactoryQuery} */
    public static QueryExecFactory of(LinkSparqlQuery link) {
        return new QueryExecFactoryOverLinkSparqlQuery(link);
    }

    public static QueryExecFactoryQuery adaptQuery(QueryExecutionFactoryQuery qef) {
        return query -> QueryExecAdapter.adapt(qef.createQueryExecution(query));
    }

    public static LinkSparqlQuery toLink(QueryExecFactoryQuery qef) {
        return new LinkSparqlQueryJenaxBase<QueryExecFactoryQuery>(qef);
    }	public static QueryExecFactoryQuery adapt(QueryExecutionFactoryQuery qef) {
        return query -> QueryExecAdapter.adapt(qef.createQueryExecution(query));
    }
}
