package org.aksw.jenax.dataaccess.sparql.linksource;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceAdapter;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.expr.ExprTransform;

public class RDFLinkSourceTransforms {
//    public static RDFLinkSourceTransform of(RDFLinkTransform transform) {
//        return new RDFLinksourceT
//    }
    public static RDFLinkSourceTransform of(RdfDataSourceTransform transform) {
        return linkSource -> {
            RDFDataSource before = RDFDataSourceAdapter.adapt(linkSource);
            RDFDataSource after = transform.apply(before);
            RDFLinkSource r = RDFLinkSourceAdapter.adapt(after);
            return r;
        };
    }

    public static RDFLinkSourceTransform of(LinkSparqlQueryTransform transform) {
        return linkSource -> RDFLinkSources.wrapWithLinkSparqlQueryTransform(linkSource, transform);
    }

    public static RDFLinkSourceTransform of(QueryTransform transform) {
        return linkSource -> RDFLinkSources.wrapWithQueryTransform(linkSource, transform);
    }

    public static RDFLinkSourceTransform of(QueryExecTransform transform) {
        return linkSource -> RDFLinkSources.wrapWithQueryExecTransform(linkSource, transform);
    }

    public static RDFLinkSourceTransform of(UpdateRequestTransform transform) {
        return linkSource -> RDFLinkSources.wrapWithUpdateTransform(linkSource, transform);
    }

    public static RDFLinkSourceTransform of(Rewrite transform) {
        return linkSource -> RDFLinkSources.wrapWithOpTransform(linkSource, transform);
    }

    public static RDFLinkSourceTransform of(SparqlStmtTransform stmtTransform) {
        return linkSource -> RDFLinkSources.wrapWithStmtTransform(linkSource, stmtTransform);
    }

    public static RDFLinkSourceTransform of(ExprTransform exprTransform) {
        return linkSource -> RDFLinkSources.wrapWithExprTransform(linkSource, exprTransform);
    }
}
