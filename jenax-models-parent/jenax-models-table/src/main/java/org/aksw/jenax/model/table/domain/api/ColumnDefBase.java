package org.aksw.jenax.model.table.domain.api;

import org.aksw.jenax.annotation.reprogen.ResourceView;

@ResourceView
public interface ColumnDefBase
    extends HasSlice
    // extends Resource
{
    // Set<ColumnDef>


//    // @Iri(NorsePrefixTerms.prefix)
//    @Iri("http://www.w3.org/2000/01/rdf-schema#label")
//    String getPrefix();
//    PrefixDefinition setPrefix(String prefix);
//
//
//    @Iri(NorsePrefixTerms.namespace)
//    @IriType
//    String getIri();
//    PrefixDefinition setIri(String namespace);
//
//    default PrefixMapping addTo(PrefixMapping pm) {
//        String prefix = getPrefix();
//        //Resource r = getIri();
//        String iri = getIri();
//        pm.setNsPrefix(prefix, iri);
//        return pm;
//    }
}
