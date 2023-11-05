package org.aksw.jenax.model.shacl.domain;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ShHasPrefixes
    extends Resource // allow direct prefix declarations using ShPrefixMapping?
{
    @Iri(ShTerms.prefixes)
    Set<ShPrefixMapping> getPrefixes();



    // FIXME Reprogen does not yet support IriTypes for keys/values
//    @Iri(NorsePrefixTerms.prefix)
//    @KeyIri(NorsePrefixTerms.prefix)
//    @ValueIri(NorsePrefixTerms.namespace)
//    Map<String, String> getMap();
}
