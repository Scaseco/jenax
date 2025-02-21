package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.io.Closeable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSourceTransform;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderTransform;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderTransformList;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSourceTransforms;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.expr.ExprTransform;

/**
 * A wrapper for an {@link RDFEngine} that supports transformations on several levels.
 * The levels are: data source, link source, link, stmt, algebra and expr.
 * All transforms are uniformly treated as transformations on the data source.
 * Each transform returns a new {@link RDFEngineDecorator} instance with the
 * data source transformation applied.
 *
 * Note, that transformations may be grouped. For example, adding multiple
 * Rewrite transformations on the algebra level will group than as to avoid
 * query-algebra-query roundtrips.
 */
public class RDFEngineDecorator<X extends RDFEngine>
{
    protected X baseEngine;
    protected List<RDFLinkBuilderTransform> linkBuilderMods = new ArrayList<>();

    // FIXME LinkSource or LinkTransform?
    protected RDFLinkTransformBuilder linkSourceTransformBuilder = new RDFLinkTransformBuilder();
    protected Deque<Closeable> closeActions = new ArrayDeque<>();

    protected RDFEngineDecorator(X baseEngine) {
        this.baseEngine = Objects.requireNonNull(baseEngine);
    }

    public static <X extends RDFEngine> RDFEngineDecorator<X> of(X baseEngine) {
        return new RDFEngineDecorator<>(baseEngine);
    }

    public RDFLinkBuilder snapshotLinkBuilder() {
        RDFLinkBuilder linkBuilder = baseEngine.newLinkBuilder();
        RDFLinkBuilderTransformList mod = new RDFLinkBuilderTransformList(new ArrayList<>(linkBuilderMods));
        RDFLinkBuilder result = mod.apply(linkBuilder);
        return result;
    }

    public RDFLink snapshotLink() {
        RDFLinkBuilder snapshotLinkBuilder = snapshotLinkBuilder();
        RDFLink baseLink = snapshotLinkBuilder.build();
        RDFLinkTransform linkMod = linkSourceTransformBuilder.build();
        RDFLink result = linkMod.apply(baseLink);
        return result;
    }

    public RDFEngineDecorator<X> addLinkBuilderMod(RDFLinkBuilderTransform linkBuilderMod) {
        // Immediately test whether the linkBuilderMod works; the builder is discarded
        RDFLinkBuilderTransformList mod = new RDFLinkBuilderTransformList(linkBuilderMods);
        RDFLinkBuilder linkBuilder = snapshotLinkBuilder();
        mod.apply(linkBuilder);

        // On success, add it
        linkBuilderMods.add(linkBuilderMod);
        return this;
    }

    public DecoratedRDFEngine<X> build() {
        RDFLinkBuilderTransformList linkBuilderMod = new RDFLinkBuilderTransformList(linkBuilderMods);
        RDFLinkTransform linkMod = linkSourceTransformBuilder.build();
        return new DecoratedRDFEngine<>(baseEngine,
                linkBuilderMod, linkMod,
                new ArrayDeque<>(closeActions));
    }

    public RDFEngineDecorator<X> addCloseAction(Closeable closeAction) {
        Objects.requireNonNull(closeAction);
        closeActions.add(closeAction);
        return this;
    }

    public RDFEngineDecorator<WrappedRDFEngine<DecoratedRDFEngine<X>>> decorate(RdfDataSourceTransform transform) {
        RDFLinkSourceTransform linkSourceTransform = RDFLinkSourceTransforms.of(transform);
        return decorate(linkSourceTransform);
    }

    public RDFEngineDecorator<WrappedRDFEngine<DecoratedRDFEngine<X>>> decorate(RDFLinkSourceTransform transform) {
        DecoratedRDFEngine<X> newBase = this.build();
        RDFLinkSource baseLinkSource = new RDFLinkSourceOverRDFEngine(newBase);
        RDFLinkSource wrappedLinkSource = transform.apply(baseLinkSource);
        WrappedRDFEngine<DecoratedRDFEngine<X>> newEngine = new WrappedRDFEngine<>(newBase, wrappedLinkSource);
        return RDFEngineDecorator.of(newEngine);
    }

    public RDFEngineDecorator<X> decorate(RDFLinkTransform transform) {
        linkSourceTransformBuilder.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(QueryTransform transform) {
        linkSourceTransformBuilder.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(QueryExecTransform transform) {
        linkSourceTransformBuilder.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(UpdateRequestTransform transform) {
        linkSourceTransformBuilder.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(LinkSparqlQueryTransform transform) {
        linkSourceTransformBuilder.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(SparqlStmtTransform transform) {
        linkSourceTransformBuilder.decorate(transform);
        return this;
    }

    // OpTransform
    // XXX Currently applied to query and update; perhaps add extra methods for either aspect.
    public RDFEngineDecorator<X> decorate(Rewrite transform) {
        linkSourceTransformBuilder.decorate(transform);
        return this;
    }

    public RDFEngineDecorator<X> decorate(ExprTransform transform) {
        linkSourceTransformBuilder.decorate(transform);
        return this;
    }
}
