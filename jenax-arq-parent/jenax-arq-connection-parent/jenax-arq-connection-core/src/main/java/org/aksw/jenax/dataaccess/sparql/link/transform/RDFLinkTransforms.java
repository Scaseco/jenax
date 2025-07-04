package org.aksw.jenax.dataaccess.sparql.link.transform;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkWrapperWithWorkerThread;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransformPaginate;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransformQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransformUpdateTransform;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.aksw.jenax.stmt.core.SparqlStmtTransforms;
import org.apache.jena.query.Query;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.expr.ExprTransform;

public class RDFLinkTransforms {
    public static RDFLinkTransform withLimit(long limit) {
        return limit == Query.NOLIMIT
                ? x -> x
                : of((Query q) -> QueryUtils.restrictToLimit(q, limit, true));
    }

    public static RDFLinkTransform withPaginate(long pageSize) {
        LinkSparqlQueryTransformPaginate queryMod = new LinkSparqlQueryTransformPaginate(pageSize);
        return of(queryMod);
    }

    public static class RDFLinkTransformAutoTxn
        implements RDFLinkTransform
    {
        @Override
        public RDFLink apply(RDFLink link) {
            return RDFLinkUtils.wrapWithAutoTxn(link, link);
        }
    }

    public static RDFLinkTransform withAutoTxn() {
        // XXX Could make singleton
        return new RDFLinkTransformAutoTxn();
    }

    public static class RDFLinkTransformWorkerThread
        implements RDFLinkTransform
    {
        @Override
        public RDFLink apply(RDFLink link) {
            return RDFLinkWrapperWithWorkerThread.wrap(link);
        }
    }

    public static RDFLinkTransform withWorkerThread() {
        return new RDFLinkTransformWorkerThread();
    }

    public static RDFLinkTransform of(SparqlStmtTransform stmtTransform) {
        return new RDFLinkTransformWithSparqlStmtTransform(stmtTransform);
    }

    public static RDFLinkTransform of(LinkSparqlQueryTransform transform) {
        return new RDFLinkTransformModular(transform, null, null);
    }

    public static RDFLinkTransform of(LinkSparqlUpdateTransform transform) {
        return new RDFLinkTransformModular(null, transform, null);
    }

    public static RDFLinkTransform of(QueryTransform transform) {
        LinkSparqlQueryTransform linkQueryTransform = new LinkSparqlQueryTransformQueryTransform(transform, null);
        return of(linkQueryTransform);
    }

    public static RDFLinkTransform of(QueryExecTransform transform) {
        LinkSparqlQueryTransform linkQueryTransform = new LinkSparqlQueryTransformQueryTransform(null, transform);
        return of(linkQueryTransform);
    }

    public static RDFLinkTransform of(UpdateRequestTransform transform) {
        LinkSparqlUpdateTransform linkUpdateTransform = new LinkSparqlUpdateTransformUpdateTransform(transform, null);
        return of(linkUpdateTransform);
    }

    public static RDFLinkTransform of(Rewrite transform) {
        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.of(transform);
        return of(stmtTransform);
    }

    public static RDFLinkTransform of(ExprTransform transform) {
        SparqlStmtTransform stmtTransform = SparqlStmtTransforms.ofExprTransform(transform);
        return of(stmtTransform);
    }
}
