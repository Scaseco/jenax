package org.aksw.jenax.dataaccess.sparql.datasource;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransforms;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.expr.ExprTransform;

public class RdfDataSourceTransforms {

    public static RDFDataSource applyLinkSourceTransform(RDFDataSource dataSource, RDFLinkSourceTransform transform) {
        RDFLinkSource oldLinkSource = dataSource.asLinkSource();
        RDFLinkSource wrappedLinkSource = transform.apply(oldLinkSource);
        RDFDataSource result = RDFDataSourceAdapter.adapt(wrappedLinkSource);
        return result;
    }

    public static RdfDataSourceTransform of(RDFLinkSourceTransform transform) {
        return dataSource -> applyLinkSourceTransform(dataSource, transform);
    }

    public static RdfDataSourceTransform of(RDFLinkTransform transform) {
        return of(transform);
    }

    public static RdfDataSourceTransform of(LinkSparqlQueryTransform transform) {
        RDFLinkSourceTransform xform = RDFLinkSourceTransforms.of(transform);
        return of(xform);
    }

    public static RdfDataSourceTransform of(QueryTransform transform) {
        RDFLinkSourceTransform xform = RDFLinkSourceTransforms.of(transform);
        return of(xform);
    }

    public static RdfDataSourceTransform of(QueryExecTransform transform) {
        RDFLinkSourceTransform xform = RDFLinkSourceTransforms.of(transform);
        return of(xform);
    }

    public static RdfDataSourceTransform of(UpdateRequestTransform transform) {
        RDFLinkSourceTransform xform = RDFLinkSourceTransforms.of(transform);
        return of(xform);
    }

    public static RdfDataSourceTransform of(Rewrite transform) {
        RDFLinkSourceTransform xform = RDFLinkSourceTransforms.of(transform);
        return of(xform);
    }

    public static RdfDataSourceTransform of(SparqlStmtTransform transform) {
        RDFLinkSourceTransform xform = RDFLinkSourceTransforms.of(transform);
        return of(xform);
    }

    public static RdfDataSourceTransform of(ExprTransform transform) {
        RDFLinkSourceTransform xform = RDFLinkSourceTransforms.of(transform);
        return of(xform);
    }
}
