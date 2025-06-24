package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFLinkDecoratorBuilder;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFLinkTransformBuilder;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.expr.ExprTransform;

public class DecoratedRDFLinkSourceBuilder
    implements RDFLinkDecoratorBuilder<DecoratedRDFLinkSourceBuilder>
{
    protected RDFLinkSource base;
    protected RDFLinkTransformBuilder transformBuilder = new RDFLinkTransformBuilder();

    public DecoratedRDFLinkSourceBuilder(RDFLinkSource base) {
        super();
        this.base = Objects.requireNonNull(base);
    }

    public RDFLinkSource getBase() {
        return base;
    }

    @Override
    public DecoratedRDFLinkSourceBuilder decorate(RDFLinkTransform transform) {
        transformBuilder.decorate(transform);
        return this;
    }

    @Override
    public DecoratedRDFLinkSourceBuilder decorate(QueryTransform transform) {
        transformBuilder.decorate(transform);
        return this;
    }

    @Override
    public DecoratedRDFLinkSourceBuilder decorate(QueryExecTransform transform) {
        transformBuilder.decorate(transform);
        return this;
    }

    @Override
    public DecoratedRDFLinkSourceBuilder decorate(UpdateRequestTransform transform) {
        transformBuilder.decorate(transform);
        return this;
    }

    @Override
    public DecoratedRDFLinkSourceBuilder decorate(LinkSparqlQueryTransform transform) {
        transformBuilder.decorate(transform);
        return this;
    }

    @Override
    public DecoratedRDFLinkSourceBuilder decorate(SparqlStmtTransform transform) {
        transformBuilder.decorate(transform);
        return this;
    }

    @Override
    public DecoratedRDFLinkSourceBuilder decorate(Rewrite transform) {
        transformBuilder.decorate(transform);
        return this;
    }

    @Override
    public DecoratedRDFLinkSourceBuilder decorate(ExprTransform transform) {
        transformBuilder.decorate(transform);
        return this;
    }

    public RDFLinkSource build() {
        RDFLinkTransform linkTransform = transformBuilder.build();
        return new RDFLinkSourceWrapperWithLinkTransform<>(base, linkTransform);
    }
}
