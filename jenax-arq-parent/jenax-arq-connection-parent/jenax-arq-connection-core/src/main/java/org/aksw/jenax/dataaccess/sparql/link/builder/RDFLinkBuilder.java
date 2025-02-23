package org.aksw.jenax.dataaccess.sparql.link.builder;

import org.aksw.commons.util.obj.HasSelf;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.apache.jena.rdflink.RDFLink;

public interface RDFLinkBuilder<X extends RDFLinkBuilder<X>>
    extends HasSelf<X>
{
    /**
     * Reqister a function with the builder that transforms
     * the result of {@link #build()}.
     */
    X linkTransform(RDFLinkTransform linkTransform);
    RDFLink build();
}
