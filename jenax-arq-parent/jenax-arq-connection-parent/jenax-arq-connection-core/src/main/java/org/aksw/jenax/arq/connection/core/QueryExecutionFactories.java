package org.aksw.jenax.arq.connection.core;

import org.aksw.jenax.arq.connection.link.QueryExecFactory;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.connection.query.QueryExecutionDecoratorBase;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

public class QueryExecutionFactories {
    public static QueryExecutionFactory adapt(QueryExecFactory queryExecFactory) {
        return new QueryExecutionFactoryOverQueryExecFactory(queryExecFactory);
    }

    public static QueryExecutionFactory of(SparqlQueryConnection conn) {
        return new QueryExecutionFactoryOverSparqlQueryConnection(conn);
    }


    public static QueryExecutionFactory of(RdfDataSource dataSource) {
        return new QueryExecutionFactoryOverRdfDataSource(dataSource);
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
            return new QueryExecutionDecoratorBase<QueryExecution>(conn.query(queryString)) {
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
            return new QueryExecutionDecoratorBase<QueryExecution>(conn.query(query)) {
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
}
