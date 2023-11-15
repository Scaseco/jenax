package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.aksw.jenax.arq.util.exec.update.UpdateExecTransform;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderDelegateBaseParse;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecBaseSelect;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecSelect;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFactoryRegistry;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryWrapperBase;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.TxnType;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.system.Txn;

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

    public static RdfDataSource applyLinkTransform(RdfDataSource rdfDataSource, RDFLinkTransform linkXform) {
        return new RdfDataSourceWrapperBase(rdfDataSource) {
            @Override
            public RDFConnection getConnection() {
                RDFConnection base = super.getConnection();
                RDFConnection r = RDFConnectionUtils.wrapWithLinkDecorator(base, linkXform);
                return r;
            }
        };
    }

    /**
     * Wrap an RdfDataSource that any update link is wrapped.
     */
    public static RdfDataSource decorateUpdate(RdfDataSource dataSource, UpdateExecTransform updateExecTransform) {
        LinkSparqlUpdateTransform componentTransform = LinkSparqlUpdateUtils.newTransform(updateExecTransform);
        RdfDataSource result = applyLinkTransform(dataSource, link -> RDFLinkUtils.apply(link, componentTransform));
        return result;
    }

    /**
     * Wrap a LinkSparqlQuery such that a possible write action is run when txn.begin() is invoked.
     * The action is run before the transaction is started.
     */
    public static RdfDataSource decorateQueryBeforeTxnBegin(RdfDataSource dataSource, Runnable action) {
        LinkSparqlQueryTransform componentTransform = link -> new LinkSparqlQueryWrapperBase(link) {
            @Override
            public void begin(TxnType type) {
                action.run();
                super.begin(type);
            }
            @Override
            public void begin(ReadWrite readWrite) {
                begin(TxnType.convert(readWrite));
            }
        };
        RdfDataSource result = applyLinkTransform(dataSource, link -> RDFLinkUtils.apply(link, componentTransform));
        return result;
    }

    /**
     * Create a {@link LinkSparqlQueryTransform} that intercepts construct query
     * requests and transforms them into select query ones.
     * The execution work is done in a {@link QueryExecBaseSelect}.
     */
    public static LinkSparqlQueryTransform execQueryViaSelect(Predicate<Query> convertToSelect) {
        LinkSparqlQueryTransform result = baseLink -> {
            return new LinkSparqlQueryWrapperBase(baseLink) {
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
                                r = QueryExecSelect.of(seenQuery, q -> delegate.query(q).build());
                            } else {
                                r = getDelegate().query(seenQuery).build();
                            }
                            return r;
                        }
                    };
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
    public static RdfDataSource execQueryViaSelect(RdfDataSource dataSource, Predicate<Query> convertToSelect) {
        LinkSparqlQueryTransform decorizer = execQueryViaSelect(convertToSelect);
        RdfDataSource result = RdfDataSources.applyLinkTransform(dataSource, link -> RDFLinkUtils.apply(link, decorizer));
        return result;
    }
}
