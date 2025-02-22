package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransformList;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransforms;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.expr.ExprTransform;

public class RDFLinkTransformBuilder
    implements RDFLinkDecoratorBuilder<RDFLinkTransformBuilder>
{
    protected List<RDFLinkTransform> mods = new ArrayList<>();

    protected LinkSparqlQueryTransformBuilder queryBuilder;
    protected LinkSparqlUpdateTransformBuilder queryBuilder;


    public boolean isEmpty() {
        return mods.isEmpty();
    }

    @Override
    public RDFLinkTransformBuilder decorate(RDFLinkTransform transform) {
        mods.add(transform);
        return this;
    }

    @Override
    public RDFLinkTransformBuilder decorate(QueryTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkTransformBuilder decorate(QueryExecTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkTransformBuilder decorate(UpdateRequestTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkTransformBuilder decorate(LinkSparqlQueryTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkTransformBuilder decorate(SparqlStmtTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    // OpTransform
    // XXX Currently applied to query and update; perhaps add extra methods for either aspect.
    @Override
    public RDFLinkTransformBuilder decorate(Rewrite transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkTransformBuilder decorate(ExprTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    public RDFLinkTransform build() {
        return new RDFLinkTransformList(new ArrayList<>(mods));
    }
}
