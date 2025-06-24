package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.op.OpTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngines;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceDecorator;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransform;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.expr.ExprTransform;

/**
 * A wrapper for an {@link RDFEngine} that supports applying transformations
 * on several levels. The levels are: link source, link, algebra and expression.
 * Internally, transformation builders are used to group transformations on the same
 * level such to minimize the amount of needed (un-)wrapping.
 */
public class RDFEngineDecorator<X extends RDFEngine>
{
    protected X baseEngine;
    protected RDFLinkSourceDecorator linkSourceDecorator;

    protected Deque<Closeable> closeActions = new ArrayDeque<>();

    /** Constructed from {@link RDFEngines#decorate(RDFEngine)}. */
    public RDFEngineDecorator(X baseEngine) {
        this.baseEngine = Objects.requireNonNull(baseEngine);
        this.linkSourceDecorator = new RDFLinkSourceDecorator(baseEngine.getLinkSource());
    }

    public RDFLinkSource snapshotLinkSource() {
        return linkSourceDecorator.snapshotLinkSource();
    }

    public RDFLinkBuilder snapshotLinkBuilder() {
        return linkSourceDecorator.snapshotLinkBuilder();
    }

    public RDFLink snapshotLink() {
        return linkSourceDecorator.snapshotLink();
    }

    public RDFEngineDecorator<X> addLinkBuilderMod(RDFLinkBuilderTransform linkBuilderMod) {
        linkSourceDecorator.addLinkBuilderMod(linkBuilderMod);
        return this;
    }

    public DecoratedRDFEngine<X> build() {
        RDFLinkSource effectiveLinkSource = linkSourceDecorator.build();
        return new DecoratedRDFEngine<>(baseEngine,
                effectiveLinkSource,
                new ArrayDeque<>(closeActions));
    }

    public RDFEngineDecorator<X> addCloseAction(Closeable closeAction) {
        Objects.requireNonNull(closeAction);
        closeActions.add(closeAction);
        return this;
    }

    public RDFEngineDecorator<X> decorate(RDFLinkSourceTransform transform) {
        linkSourceDecorator.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(RDFLinkTransform transform) {
        linkSourceDecorator.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(QueryTransform transform) {
        linkSourceDecorator.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(QueryExecTransform transform) {
        linkSourceDecorator.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(UpdateRequestTransform transform) {
        linkSourceDecorator.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(LinkSparqlQueryTransform transform) {
        linkSourceDecorator.decorate(transform);
        return this;
    }

//    public RDFEngineDecorator<X> decorate(SparqlStmtTransform transform) {
//    	linkSourceDecorator.decorate(transform);
//        return this;
//    }

    // OpTransform
    // XXX Currently applied to query and update; perhaps add extra methods for either aspect.
    public RDFEngineDecorator<X> decorate(OpTransform transform) {
        linkSourceDecorator.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(ExprTransform transform) {
        linkSourceDecorator.decorate(transform);
        return this;
    }
}
