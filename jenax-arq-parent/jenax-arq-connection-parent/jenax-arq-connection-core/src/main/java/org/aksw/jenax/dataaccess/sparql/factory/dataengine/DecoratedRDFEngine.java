package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.io.Closeable;
import java.util.Deque;
import java.util.Objects;
import java.util.Optional;

import org.aksw.commons.util.exception.FinallyRunAll;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.ServiceControl;
import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;

/**
 * A wrapper for a base RDF engine with an immutable set of decorators
 * that are applied to  link builders and links.
 */
public class DecoratedRDFEngine<X extends RDFEngine>
    implements RDFEngine
{
    protected X baseEngine;
    protected RDFLinkSource effectiveLinkSource;
    protected Deque<Closeable> closeActions;

    public DecoratedRDFEngine(X baseEngine,
            RDFLinkSource effectiveLinkSource,
            Deque<Closeable> closeActions) {
        super();
        this.baseEngine = Objects.requireNonNull(baseEngine);
        this.effectiveLinkSource = Objects.requireNonNull(effectiveLinkSource);
        this.closeActions = Objects.requireNonNull(closeActions);
    }

    /** Get the base engine. Never null. */
    public X getBaseEngine() {
        return baseEngine;
    }

    @Override
    public Optional<ServiceControl> getServiceControl() {
        return baseEngine.getServiceControl();
    }

    @Override
    public RDFLinkSource getLinkSource() {
        return effectiveLinkSource;
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

//@Override
//public RDFLinkBuilder newLinkBuilder() {
//  X baseEngine = getBaseEngine();
//
//  RDFLinkBuilder baseLinkBuilder  = baseEngine.newLinkBuilder();
//  RDFLinkBuilder finalLinkBuilder = (linkBuilderMod == null)
//          ? baseLinkBuilder
//          : linkBuilderMod.apply(baseLinkBuilder);
//
//  RDFLinkBuilder wrapperLinkBuilder = new RDFLinkBuilderOverLinkSupplier(() -> {
//      RDFLink baseLink = finalLinkBuilder.build();
//      RDFLink finalLink = linkMod == null
//              ? baseLink
//              : linkMod.apply(baseLink);
//      return finalLink;
//  });
//
//  return wrapperLinkBuilder;
//}

/** Return the engine's underlying dataset; null if there is none. */
//@Override
//public DatasetGraph getDataset() {
//return linkSource.getDataset();
//}

