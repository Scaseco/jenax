package org.aksw.jenax.dataaccess.sparql.link.builder;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.jena.rdflink.RDFLink;

public class RDFLinkBuilderOverLinkSupplier
    implements RDFLinkBuilder
{
    protected Supplier<RDFLink> linkSupplier;

    public RDFLinkBuilderOverLinkSupplier(Supplier<RDFLink> linkSupplier) {
        super();
        this.linkSupplier = Objects.requireNonNull(linkSupplier);
    }

    @Override
    public RDFLink build() {
        return linkSupplier.get();
    }
}
