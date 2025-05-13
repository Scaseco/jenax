package org.aksw.jenax.dataaccess.sparql.linksource;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilder;
import org.aksw.jenax.dataaccess.sparql.link.builder.RDFLinkBuilderTransform;

public class RDFLinkSourceTransformLinkBuilder
    implements RDFLinkSourceTransform
{
    protected RDFLinkBuilderTransform transform;

    public RDFLinkSourceTransformLinkBuilder(RDFLinkBuilderTransform transform) {
        super();
        this.transform = Objects.requireNonNull(transform);
    }

    @Override
    public RDFLinkSource apply(RDFLinkSource t) {
        return new RDFLinkSourceWrapperBase<>(t) {
            @Override
            public RDFLinkBuilder<?> newLinkBuilder() {
                RDFLinkBuilder<?> base = super.newLinkBuilder();
                RDFLinkBuilder<?> result = transform.apply(base);
                return result;
            }
        };
    }

    @Override
    public String toString() {
        return "RDFLinkSourceTransformLinkBuilder [transform=" + transform + "]";
    }
}
