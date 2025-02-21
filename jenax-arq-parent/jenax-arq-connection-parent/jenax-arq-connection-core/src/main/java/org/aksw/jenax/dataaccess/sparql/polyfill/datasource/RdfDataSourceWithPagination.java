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
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceWrapperBase;
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

//public class RdfDataSourceWithPagination
//    extends RDFDataSourceWrapperBase<RDFDataSource>
//{
//    protected long pageSize;
//
//    public RdfDataSourceWithPagination(RDFDataSource delegate, long pageSize) {
//        super(delegate);
//        this.pageSize = pageSize;
//    }
//
//    @Override
//    public RDFConnection getConnection() {
//        RDFConnection base = super.getConnection();
//
//        LinkSparqlQueryTransform queryLinkTransform = link -> new LinkSparqlQueryWrapperBase(link) {
//            @Override
//            public QueryExecBuilder newQuery() {
//                return execSelectPaginated(link, pageSize);
//            }
//        };
//
//        return RDFConnectionUtils.wrapWithLinkTransform(base, link -> RDFLinkUtils.wrapWithQueryLinkTransform(link, queryLinkTransform));
//    }
//}
