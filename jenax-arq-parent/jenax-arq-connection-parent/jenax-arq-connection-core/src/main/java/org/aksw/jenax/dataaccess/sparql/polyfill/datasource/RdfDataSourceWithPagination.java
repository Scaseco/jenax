package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.aksw.jenax.arq.util.binding.QueryIterOverQueryExec;
import org.aksw.jenax.arq.util.binding.QueryIterOverQueryIteratorSupplier;
import org.aksw.jenax.arq.util.binding.QueryIteratorCount;
import org.aksw.jenax.arq.util.exec.query.PaginationQueryIterator;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecBaseSelect;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecOverRowSet.QueryExecOverRowSetInternal;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryWrapperBase;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.RowSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdfDataSourceWithPagination
    extends RdfDataSourceWrapperBase<RdfDataSource>
{
    protected long pageSize;

    public RdfDataSourceWithPagination(RdfDataSource delegate, long pageSize) {
        super(delegate);
        this.pageSize = pageSize;
    }

    @Override
    public RDFConnection getConnection() {
        RDFConnection base = super.getConnection();

        LinkSparqlQueryTransform queryLinkTransform = link -> new LinkSparqlQueryWrapperBase(link) {
            @Override
            public QueryExecBuilder newQuery() {
                return execSelectPaginated(link, pageSize);
            }
        };

        return RDFConnectionUtils.wrapWithLinkTransform(base, link -> RDFLinkUtils.wrapWithQueryLinkTransform(link, queryLinkTransform));
    }

    public static class QueryIterPaginated
        extends QueryIterOverQueryIteratorSupplier<QueryIteratorCount>
    {
        private static final Logger logger = LoggerFactory.getLogger(QueryIterPaginated.class);

        protected static final AtomicLong idGenerator = new AtomicLong();

        protected Query originalQuery;
        protected PaginationQueryIterator queryIt;
        protected Supplier<QueryExecBuilder> queryExecBuilderSupplier;

        /** The execution id only exists to make it more easy to relate log messages.
         *  The id is initialized lazily upon invocation of {@link #nextQueryIterator()}. */
        protected long execId = -1;

        public QueryIterPaginated(Query originalQuery, PaginationQueryIterator queryIt, Supplier<QueryExecBuilder> queryExecBuilderSupplier) {
            super(null);
            this.originalQuery = originalQuery;
            this.queryIt = queryIt;
            this.queryExecBuilderSupplier = queryExecBuilderSupplier;
        }

        @Override
        protected QueryIteratorCount nextQueryIterator() {
            if (execId == -1) {
                execId = idGenerator.getAndUpdate(x -> x == Long.MAX_VALUE ? 0 : x + 1);
                if (logger.isInfoEnabled()) {
                    logger.info("Paginated execution #" + execId + " started:\n"+ originalQuery);
                }
            }

            long pageSize = queryIt.getPageSize();

            // If there was no prior page then set the lastItemCount to the pageSize
            // Stop as soon as lastItemCount is less than the lastItemCount.
            long lastItemCount = currentIt == null ? pageSize : currentIt.getCounter();
            QueryIteratorCount result = null;
            if (!(lastItemCount < pageSize) && queryIt.hasNext()) {
                Query query = queryIt.next();
                if (query != null) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Paginated execution #" + execId + " processing range: " + QueryUtils.toRange(query));
                    }
                    QueryExecBuilder queryExecBuilder = queryExecBuilderSupplier.get();
                    QueryExec queryExec = queryExecBuilder.query(query).build();
                    result = new QueryIteratorCount(new QueryIterOverQueryExec(getExecContext(), queryExec));
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Paginated execution #" + execId + " finished (consumed).");
                }
            }
            return result;
        }

        @Override
        protected void closeIteratorActual() {
            if (execId >= 0 && !isFinished) {
                if (logger.isInfoEnabled()) {
                    logger.info("Paginated execution #" + execId + " finished (closed).");
                }
            }
            super.closeIteratorActual();
        }

        public static QueryIterPaginated of(Query query, long pageSize, Supplier<QueryExecBuilder> queryExecBuilderSupplier) {
            Query clone = query.cloneQuery();
            PaginationQueryIterator queryIt = new PaginationQueryIterator(clone, pageSize);
            return new QueryIterPaginated(query, queryIt, queryExecBuilderSupplier);
        }
    }

    public static QueryExecBuilder execSelectPaginated(LinkSparqlQuery baseLink, long pageSize) {
        return new QueryExecBuilderCustomBase<>() {
            @Override
            public QueryExec build() {
                // Create a copy of the builder state from which we reuse the settings
                QueryExecBuilderCustomBase<?> prototype = new QueryExecBuilderCustomBase<>(this) {
                    @Override
                    public QueryExec build() {
                        throw new UnsupportedOperationException("should never be called");
                    }
                };

                Query parsedQuery = getParsedQuery();
                Objects.requireNonNull(parsedQuery, "Query not set");
                QueryExec r = new QueryExecBaseSelect(parsedQuery) {
                    @Override
                    protected QueryExec doSelect(Query selectQuery) {
                        List<Var> vars = selectQuery.getProjectVars();

                        // Apply any settings from (the prototype derived from) this builder to all query builders
                        // created from the underlying baseLink.
                        Supplier<QueryExecBuilder> queryExecBuilderSupplier = () -> prototype.applySettings(baseLink.newQuery());
                        QueryIterator qIter = QueryIterPaginated.of(selectQuery, pageSize, queryExecBuilderSupplier);
                        RowSet rowSet = RowSet.create(qIter, vars);
                        return new QueryExecOverRowSetInternal(rowSet);
                    }
                };
                return r;
            }
        };
    }
}
