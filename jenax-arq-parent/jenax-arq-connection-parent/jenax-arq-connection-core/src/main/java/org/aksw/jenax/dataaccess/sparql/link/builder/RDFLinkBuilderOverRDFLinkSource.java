package org.aksw.jenax.dataaccess.sparql.link.builder;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.linksource.RDFLinkSource;
import org.apache.jena.rdflink.RDFLink;

public class RDFLinkBuilderOverRDFLinkSource<X extends RDFLinkBuilderOverRDFLinkSource<X>>
    extends RDFLinkBuilderBase<X>
{
    protected RDFLinkSource linkSource;

    public RDFLinkBuilderOverRDFLinkSource(RDFLinkSource linkSource) {
        super();
        this.linkSource = Objects.requireNonNull(linkSource);
    }

    public RDFLinkSource getLinkSource() {
        return linkSource;
    }

    @Override
    public RDFLink buildBaseLink() {
        RDFLink result = linkSource.newLink();
        return result;
    }
}
