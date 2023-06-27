package org.aksw.jenax.model.shacl.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

@ResourceView
public interface ShPrefixMapping
    extends Resource
{
    @Iri("http://www.w3.org/2002/07/owl#import")
    Set<ShPrefixMapping> getOwlImports();

    @Iri(ShaclTerms.declare)
    Set<PrefixDeclaration> getPrefixDeclarations();

    default ShPrefixMapping put(String prefix, String value) {
        Set<PrefixDeclaration> set = getPrefixDeclarations();
        boolean done = false;
        for(PrefixDeclaration def : set) {
            String p = def.getPrefix();
            if (Objects.equals(prefix, prefix)) {
                def.setIri(value);
                done = true;
                break;
            }
        }

        if (!done) {
            PrefixDeclaration n = getModel().createResource().as(PrefixDeclaration.class).setPrefix(prefix).setIri(value);
            set.add(n);
        }

        return this;
    }

    default Map<String, String> getMap() {
        Map<String, String> result = new HashMap<>();
        Set<PrefixDeclaration> set = getPrefixDeclarations();
        for(PrefixDeclaration def : set) {
            String prefix = def.getPrefix();
            String namespace = def.getIri();
            result.put(prefix, namespace);
        }
        return result;
    }

    default PrefixMapping addTo(PrefixMapping pm) {
        Set<PrefixDeclaration> set = getPrefixDeclarations();
        for(PrefixDeclaration def : set) {
            String prefix = def.getPrefix();
            // Resource r = def.getIri();
            String iri = def.getIri();
            pm.setNsPrefix(prefix, iri);
        }

        return pm;
    }
}
