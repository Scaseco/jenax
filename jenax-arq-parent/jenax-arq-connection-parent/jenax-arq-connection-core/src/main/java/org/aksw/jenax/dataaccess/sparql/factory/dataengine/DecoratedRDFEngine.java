package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.io.Closeable;
import java.util.Deque;
import java.util.Objects;

import org.aksw.commons.util.exception.FinallyRunAll;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSourceAdapter;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderOverLinkSupplier;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;


/**
 * A wrapper for a base RDF engine with an immutable set of decorators
 * that are applied to  link builders and links.
 */
public class DecoratedRDFEngine<X extends RDFEngine>
    implements RDFEngine
{
    protected X baseEngine;
    protected RDFLinkBuilderTransform linkBuilderMod;
    protected RDFLinkTransform linkMod;
    protected Deque<Closeable> closeActions;

    protected RDFDataSource dataSource;

    public DecoratedRDFEngine(X baseEngine,
            RDFLinkBuilderTransform linkBuilderMod, RDFLinkTransform linkMod,
            Deque<Closeable> closeActions) {
        super();
        this.baseEngine = Objects.requireNonNull(baseEngine);
        this.linkBuilderMod = linkBuilderMod;
        this.linkMod = linkMod;
        this.closeActions = closeActions;

        this.dataSource = RDFDataSourceAdapter.adapt(
            RDFLinkSourceOverRDFEngine.of(this));
    }

    /** Return the engine's underlying dataset; null if there is none. */
    @Override
    public DatasetGraph getDataset() {
        return baseEngine.getDataset();
    }

    /** Get the base engine. Never null. */
    public X getBaseEngine() {
        return baseEngine;
    }

    /**
     * Get the data source view of this engine. Never null.
     *
     * The returned data source is backed by {@link #newLinkBuilder()} and
     * {@link #getDataset()}.
     *
     * Use {@link RDFDataSource#asLinkSource()} to obtain a {@link RDFLinkSource} view.
     */
    public RDFDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public RDFLinkBuilder newLinkBuilder() {
        X baseEngine = getBaseEngine();

        RDFLinkBuilder baseLinkBuilder  = baseEngine.newLinkBuilder();
        RDFLinkBuilder finalLinkBuilder = (linkBuilderMod == null)
                ? baseLinkBuilder
                : linkBuilderMod.apply(baseLinkBuilder);

        RDFLinkBuilder wrapperLinkBuilder = new RDFLinkBuilderOverLinkSupplier(() -> {
            RDFLink baseLink = finalLinkBuilder.build();
            RDFLink finalLink = linkMod == null
                    ? baseLink
                    : linkMod.apply(baseLink);
            return finalLink;
        });

        return wrapperLinkBuilder;
    }

    @Override
    public void close() throws Exception {
        try {
            FinallyRunAll closer = FinallyRunAll.create();
            for (Closeable closeAction : closeActions) {
                closer.addThrowing(closeAction::close);
            }
            closer.run();
        } finally {
            baseEngine.close();
        }
    }
}
