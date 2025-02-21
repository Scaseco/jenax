package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.apache.jena.rdflink.RDFLink;

public class DecoratedRDFLinkSource<X extends RDFLinkSource>
    extends RDFLinkSourceWrapperBase<X>
{
    protected List<RDFLinkTransform> mods = new ArrayList<>();

    public DecoratedRDFLinkSource(X delegate) {
        super(delegate);
    }

    public void addMod(RDFLinkTransform mod) {
        mods.add(mod);
    }

    @Override
    public RDFLink newLink() {
        RDFLink result = super.newLink();
        for (RDFLinkTransform mod : mods) {
            RDFLink next = mod.apply(result);
            result = next;
        }
        return result;
    }
}
