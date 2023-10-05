package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderDelegateBaseParse;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceDelegateBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecBaseSelect;
import org.aksw.jenax.dataaccess.sparql.exec.query.RowSetDelegateBase;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFactoryRegistry;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTmp;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.system.Txn;

import com.google.common.base.Preconditions;

public class RdfDataSources {

    /** Execute a query and invoke a function on the response.
     * Upon returning the internally freshly obtained connection and query execution are closed so
     * the result must be detached from those resources.
     */
    public static <T> T exec(RdfDataSource dataSource, Query query, Function<? super QueryExecution, T> qeToResult) {
        Object[] tmp = new Object[] { null };
        try (RDFConnection conn = dataSource.getConnection()) {
            Txn.executeRead(conn, () -> {
                try (QueryExecution qe = conn.query(query)) {
                    tmp[0] = qeToResult.apply(qe);
                }
            });
        }
        @SuppressWarnings("unchecked")
        T result = (T)tmp[0];
        return result;
    }

    /** Reads the 'engine' attribute from the options (if absent defaults to 'mem')
     *  and instantiates the appropriate data source - if possible */
    public static RdfDataEngine setupRdfDataSource(Map<String, Object> options) throws Exception {
        RdfDataSourceSpecBasicFromMap spec = RdfDataSourceSpecBasicFromMap.wrap(options);

        String sourceType = Optional.ofNullable(spec.getEngine()).orElse("mem");

        RdfDataEngineFactory factory = RdfDataEngineFactoryRegistry.get().getFactory(sourceType);
        if (factory == null) {
            throw new RuntimeException("No RdfDataSourceFactory registered under name " + sourceType);
        }


        RdfDataEngine result = factory.create(options);
        return result;
    }

    public static RdfDataSource applyLinkTransform(RdfDataSource rdfDataSource, Function<? super RDFLink, ? extends RDFLink> linkXform) {
        return new RdfDataSourceDelegateBase(rdfDataSource) {
            @Override
            public RDFConnection getConnection() {
                RDFConnection base = super.getConnection();
                RDFConnection r = RDFConnectionUtils.wrapWithLinkDecorator(base, linkXform);
                return r;
            }
        };
    }

    /**
     * Create a {@link LinkSparqlQueryTransform} that intercepts construct query
     * requests and transforms them into select query ones.
     * The execution work is done in a {@link QueryExecBaseSelect}.
     */
    public static LinkSparqlQueryTransform execAsSelectDecorizer(Predicate<Query> convertToSelect) {
        LinkSparqlQueryTransform result = baseLink -> {
            return new LinkSparqlQueryTmp() {
                @Override
                public QueryExecBuilder newQuery() {
                    return new QueryExecBuilderDelegateBaseParse(baseLink.newQuery()) {
                        protected Query seenQuery = null;
                        @Override
                        public QueryExecBuilder query(Query query) {
                            seenQuery = query;
                            return this;
                        }
                        @Override
                        public QueryExec build() {
                            Objects.requireNonNull(seenQuery, "Query not set");
                            QueryExec r;
                            boolean doConvert = convertToSelect.test(seenQuery);
                            if (doConvert) {
                                r = new QueryExecBaseSelect(seenQuery, getContext()) {
                                    @Override
                                    protected RowSet createRowSet(Query selectQuery) {
                                        QueryExec baseExec = delegate.query(selectQuery).build();
                                        RowSet s = new RowSetDelegateBase(baseExec.select()) {
                                            @Override
                                            public void close() {
                                                baseExec.close();
                                            }
                                        };
                                        return s;
                                    }
                                };
                            } else {
                                r = delegate.query(seenQuery).build();
                            }
                            return r;
                        }
                    };
                }

                @Override
                public void close() {
                    baseLink.close();
                }

                @Override
                public Transactional getDelegate() {
                    return baseLink;
                }
            };
        };
        return result;
    }

    /**
     * Wrap a data source such that matching queries are transformed to and executed as SELECT queries.
     * The select query is passed on to the underlying data source, whereas the row set
     * is post processed locally to fulfill the original request.
     *
     * @param dataSource The data source which to wrap
     * @param convertToSelect Only matching queries are executed as select
     * @return
     */
    public static RdfDataSource execAsSelect(RdfDataSource dataSource, Predicate<Query> convertToSelect) {
        LinkSparqlQueryTransform decorizer = execAsSelectDecorizer(convertToSelect);
        RdfDataSource result = RdfDataSources.applyLinkTransform(dataSource, link -> RDFLinkUtils.apply(link, decorizer));
        return result;
    }
}
