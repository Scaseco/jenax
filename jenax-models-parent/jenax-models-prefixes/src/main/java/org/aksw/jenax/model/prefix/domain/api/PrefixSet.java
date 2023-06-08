package org.aksw.jenax.model.prefix.domain.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

@ResourceView
public interface PrefixSet
    extends Resource
{
    @Iri(NorsePrefixTerms.prefix)
    Set<PrefixDefinition> getDefinitions();

    // FIXME Reprogen does not yet support IriTypes for keys/values
//    @Iri(NorsePrefixTerms.prefix)
//    @KeyIri(NorsePrefixTerms.prefix)
//    @ValueIri(NorsePrefixTerms.namespace)
//    Map<String, String> getMap();

    default PrefixSet put(String prefix, String value) {
        Set<PrefixDefinition> set = getDefinitions();
        boolean done = false;
        for(PrefixDefinition def : set) {
            String p = def.getPrefix();
            if (Objects.equals(prefix, prefix)) {
                def.setIri(value);
                done = true;
                break;
            }
        }

        if (!done) {
            PrefixDefinition n = getModel().createResource().as(PrefixDefinition.class).setPrefix(prefix).setIri(value);
            set.add(n);
        }

        return this;
    }

    default Map<String, String> getMap() {
        Map<String, String> result = new HashMap<>();
        Set<PrefixDefinition> set = getDefinitions();
        for(PrefixDefinition def : set) {
            String prefix = def.getPrefix();
            String namespace = def.getIri();
            result.put(prefix, namespace);
        }
        return result;
    }

    default PrefixMapping addTo(PrefixMapping pm) {
        Set<PrefixDefinition> set = getDefinitions();
        for(PrefixDefinition def : set) {
            String prefix = def.getPrefix();
            // Resource r = def.getIri();
            String iri = def.getIri();
            pm.setNsPrefix(prefix, iri);
        }

        return pm;
    }
}
