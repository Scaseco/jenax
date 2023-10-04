package org.aksw.jenax.model.shacl.domain;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

/** An individual prefix declaration. */
@ResourceView
public interface ShPrefixDeclaration
    extends Resource
{
    @Iri("rdfs:label")
    String getName();

    @Iri(ShTerms.prefix)
    String getPrefix();
    ShPrefixDeclaration setPrefix(String prefix);


    // Namespaces are not IRIs but literals of type xsd:anyURI
    @Iri(ShTerms.namespace)
    // @IriType
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
