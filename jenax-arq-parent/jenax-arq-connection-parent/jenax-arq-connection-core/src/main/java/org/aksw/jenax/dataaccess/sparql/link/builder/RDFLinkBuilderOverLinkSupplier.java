package org.aksw.jenax.dataaccess.sparql.link.builder;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.jena.rdflink.RDFLink;

/**
 * A link builder without any configurable properties.
 * It returns links from the given supplier.
 */
@Deprecated // Avoid lambda-based transformations because they are hard to debug.
public class RDFLinkBuilderOverLinkSupplier<X extends RDFLinkBuilderOverLinkSupplier<X>>
    extends RDFLinkBuilderBase<X>
{
    protected Supplier<RDFLink> linkSupplier;

    public RDFLinkBuilderOverLinkSupplier(Supplier<RDFLink> linkSupplier) {
        super();
        this.linkSupplier = Objects.requireNonNull(linkSupplier);
    }

    @Override
    public RDFLink buildBaseLink() {
        return linkSupplier.get();
    }

    @Override
    public String toString() {
        return "RDFLinkBuilderOverLinkSupplier [linkSupplier=" + linkSupplier + "]";
    }
}
