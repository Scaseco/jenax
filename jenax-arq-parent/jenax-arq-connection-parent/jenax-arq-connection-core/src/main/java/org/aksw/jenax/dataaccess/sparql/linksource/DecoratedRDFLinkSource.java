package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * FIXME Confusing design:
 *   Note: The delegate is the original data source in order to allow unwrapping it with getDelegate().
 *   Calls on this class are actually forwarded to effectiveLinkSource.
 *   So there is a difference between the delegate (which object to delegate calls to) and the wrapped object.
 *
 */
public class DecoratedRDFLinkSource<X extends RDFLinkSource>
    extends RDFLinkSourceWrapperBase<X>
{
    protected RDFLinkSource effectiveLinkSource;

    public DecoratedRDFLinkSource(X delegate, RDFLinkSource effectiveLinkSource) {
        super(delegate);
        this.effectiveLinkSource = Objects.requireNonNull(effectiveLinkSource);
    }

    @Override
    public DatasetGraph getDatasetGraph() {
        return effectiveLinkSource.getDatasetGraph();
    }

    @Override
    public RDFLinkBuilder<?> newLinkBuilder() {
        RDFLinkBuilder<?> result = effectiveLinkSource.newLinkBuilder();
        return result;
    }

    @Override
    public String toString() {
        return "DecoratedRDFLinkSource [effectiveLinkSource=" + effectiveLinkSource + ", getDelegate()=" + getDelegate() + "]";
    }

//    @Override
//    public RDFLink newLink() {
//        RDFLink result = effectiveLinkSource.newLink();
//        return result;
////        RDFLink result = super.newLink();
////        for (RDFLinkTransform mod : mods) {
////            RDFLink next = mod.apply(result);
////            result = next;
////        }
////        return result;
//    }
}
