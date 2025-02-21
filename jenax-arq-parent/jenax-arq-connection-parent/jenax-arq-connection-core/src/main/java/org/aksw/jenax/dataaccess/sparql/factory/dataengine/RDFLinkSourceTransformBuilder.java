package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransforms;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransformFromLinkTransform;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.expr.ExprTransform;

public class RDFLinkSourceTransformBuilder
    implements RDFLinkDecoratorBuilder<RDFLinkSourceTransformBuilder>
{
    protected List<RDFLinkSourceTransform> mods = new ArrayList<>();

    protected RDFLinkTransformBuilder openBuilder = null;

    protected void ensureOpenBuilder() {
        if (openBuilder == null) {
            openBuilder = new RDFLinkTransformBuilder();
        }
    }

    protected void closeLinkBuilder() {
        if (openBuilder != null) {
            if (!openBuilder.isEmpty()) {
                RDFLinkTransform linkTransform = openBuilder.build();
                RDFLinkSourceTransform tmp = new RDFLinkSourceTransformFromLinkTransform(linkTransform);
                mods.add(tmp);
            }
            openBuilder = null;
        }
    }

    public RDFLinkSourceTransformBuilder decorate(RDFLinkSourceTransform transform) {
        closeLinkBuilder();
        mods.add(transform);
        return this;
    }

    @Override
    public RDFLinkSourceTransformBuilder decorate(RDFLinkTransform transform) {
        RDFLinkSourceTransform tmp = new RDFLinkSourceTransformFromLinkTransform(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkSourceTransformBuilder decorate(QueryTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkSourceTransformBuilder decorate(QueryExecTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkSourceTransformBuilder decorate(UpdateRequestTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkSourceTransformBuilder decorate(LinkSparqlQueryTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkSourceTransformBuilder decorate(SparqlStmtTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    // OpTransform
    // XXX Currently applied to query and update; perhaps add extra methods for either aspect.
    @Override
    public RDFLinkSourceTransformBuilder decorate(Rewrite transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    @Override
    public RDFLinkSourceTransformBuilder decorate(ExprTransform transform) {
        RDFLinkTransform tmp = RDFLinkTransforms.of(transform);
        return decorate(tmp);
    }

    public RDFLinkSourceTransform build() {
        return new RDFLinkSourceTransformList(mods);
    }
}
