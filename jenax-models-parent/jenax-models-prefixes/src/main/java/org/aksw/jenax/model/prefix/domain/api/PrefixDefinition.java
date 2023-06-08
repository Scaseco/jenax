package org.aksw.jenax.model.prefix.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

@ResourceView
public interface PrefixDefinition
    extends Resource
{
    // @Iri(NorsePrefixTerms.prefix)
    @Iri("http://www.w3.org/2000/01/rdf-schema#label")
    String getPrefix();
    PrefixDefinition setPrefix(String prefix);


    @Iri(NorsePrefixTerms.namespace)
    @IriType
    String getIri();
    PrefixDefinition setIri(String namespace);

    default PrefixMapping addTo(PrefixMapping pm) {
        String prefix = getPrefix();
        //Resource r = getIri();
        String iri = getIri();
        pm.setNsPrefix(prefix, iri);
        return pm;
    }
}
