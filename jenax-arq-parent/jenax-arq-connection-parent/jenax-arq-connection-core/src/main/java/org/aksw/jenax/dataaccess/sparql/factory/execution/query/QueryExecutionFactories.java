package org.aksw.jenax.dataaccess.sparql.factory.execution.query;

import org.aksw.jenax.arq.util.exec.query.QueryExecutionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactory;
import org.aksw.jenax.dataaccess.sparql.execution.query.QueryExecutionWrapperBase;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngines;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class QueryExecutionFactories {
    public static QueryExecutionFactory empty() {
        return of(DatasetFactory.empty());
    }

    public static QueryExecutionFactory adapt(QueryExecFactory queryExecFactory) {
        return new QueryExecutionFactoryOverQueryExecFactory(queryExecFactory);
    }

    public static QueryExecutionFactory of(SparqlQueryConnection conn) {
        return new QueryExecutionFactoryOverSparqlQueryConnection(conn);
    }

    public static QueryExecutionFactory of(RdfDataSource dataSource) {
        return new QueryExecutionFactoryOverRdfDataSource(dataSource);
    }

    /** Create a {@link QueryExecutionFactory} over a {@link Dataset} */
    public static QueryExecutionFactory of(Dataset dataset) {
        return of(RdfDataEngines.of(dataset));
    }

    public static class QueryExecutionFactoryOverRdfDataSource
        implements QueryExecutionFactory
    {
        protected RdfDataSource dataSource;

        public QueryExecutionFactoryOverRdfDataSource(RdfDataSource decoratee) {
            super();
            this.dataSource = decoratee;
        }

        @Override
        public QueryExecution createQueryExecution(String queryString) {
            RDFConnection conn = dataSource.getConnection();
            return new QueryExecutionWrapperBase<QueryExecution>(conn.query(queryString)) {
                @Override
                public void close() {
                    try {
                        super.close();
                    } finally {
                        conn.close();
                    }
                }
            };
        }

        @Override
        public QueryExecution createQueryExecution(Query query) {
            RDFConnection conn = dataSource.getConnection();
            return new QueryExecutionWrapperBase<QueryExecution>(conn.query(query)) {
                @Override
                public void close() {
                    try {
                        super.close();
                    } finally {
                        conn.close();
                    }
                }
            };
        }

        @Override
        public void close() throws Exception {
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getState() {
            return null;
        }

        @Override
        public <T> T unwrap(Class<T> clazz) {
            return null;
            // Descend with unwrap into DataSource?
//		    @SuppressWarnings("unchecked")
//		    @Override
//		    public <T> T unwrap(Class<T> clazz) {
//		        T result = getClass().isAssignableFrom(clazz)
//					? (T)this
//					: decoratee.unwrap(clazz);
//
//		        return result;
//		    }
        }
    }

    protected static final Query datasetHashQuery = SparqlStmtMgr.loadQuery("probe-dataset-hash-simple.rq");

    public static String fetchDatasetHash(QueryExecutionFactoryQuery qef) {
        String result = QueryExecutionUtils.fetchNode(qef::createQueryExecution, datasetHashQuery)
                .map(Node::getLiteralLexicalForm)
                .map(String::toLowerCase)
                .orElse(null);
        return result;
    }
}
