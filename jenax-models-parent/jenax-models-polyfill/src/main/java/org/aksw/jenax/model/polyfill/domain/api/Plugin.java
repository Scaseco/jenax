package org.aksw.jenax.model.polyfill.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface Plugin
    extends Resource
{
    @Iri(PolyfillTerms.javaClass)
    String getClassName();
    Plugin setClassName(String name);

    @Iri(PolyfillTerms.profile)
    Set<String> getRecommendedProfiles();

//    @Iri(PolyfillTerms.isEnabled)
//    String isEnabled();
//    Plugin setEnabled(String name);

}
