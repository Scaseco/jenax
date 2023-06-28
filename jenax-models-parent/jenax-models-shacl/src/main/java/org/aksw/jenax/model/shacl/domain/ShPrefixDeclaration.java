package org.aksw.jenax.model.shacl.domain;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

/** An individual prefix declaration. */
@ResourceView
public interface ShPrefixDeclaration
    extends Resource
{
    @Iri(ShaclTerms.prefix)
    String getPrefix();
    ShPrefixDeclaration setPrefix(String prefix);


    @Iri(ShaclTerms.namespace)
    @IriType
    String getIri();
    ShPrefixDeclaration setIri(String namespace);

    default PrefixMapping addTo(PrefixMapping pm) {
        String prefix = getPrefix();
        //Resource r = getIri();
        String iri = getIri();
        pm.setNsPrefix(prefix, iri);
        return pm;
    }
}
