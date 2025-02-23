package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.apache.jena.rdflink.RDFLink;

public class DecoratedRDFLinkSource<X extends RDFLinkSource>
    extends RDFLinkSourceWrapperBase<X>
{
    protected RDFLinkSource effectiveLinkSource;

    public DecoratedRDFLinkSource(X delegate, RDFLinkSource effectiveLinkSource) {
        super(delegate);
        this.effectiveLinkSource = Objects.requireNonNull(effectiveLinkSource);
    }

    @Override
    public RDFLink newLink() {
        RDFLink result = effectiveLinkSource.newLink();
        return result;
//        RDFLink result = super.newLink();
//        for (RDFLinkTransform mod : mods) {
//            RDFLink next = mod.apply(result);
//            result = next;
//        }
//        return result;
    }
}
