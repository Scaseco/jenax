package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.expr.ExprTransform;

/**
 * Interface to apply transformations on different levels (link, query/update, algebra, exec).
 * Also used to ensure consistency between the decorator APIs for
 * link sources, links and link transforms.
 */
public interface RDFLinkDecoratorBuilder<X extends RDFLinkDecoratorBuilder<X>> {
    X decorate(RDFLinkTransform transform);
    X decorate(QueryTransform transform);
    X decorate(QueryExecTransform transform);
    X decorate(UpdateRequestTransform transform);
    X decorate(LinkSparqlQueryTransform transform);
    X decorate(SparqlStmtTransform transform);
    X decorate(Rewrite transform);
    X decorate(ExprTransform transform);
}
