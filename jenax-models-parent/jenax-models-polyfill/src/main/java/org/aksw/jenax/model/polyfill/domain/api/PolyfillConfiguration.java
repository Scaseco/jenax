package org.aksw.jenax.model.polyfill.domain.api;

import java.util.List;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface PolyfillConfiguration
    extends Resource
{
    @Iri(PolyfillTerms.profile)
    String getProfile();
    PolyfillConfiguration setProfile(String profile);

    @Iri(PolyfillTerms.plugins)
    List<Plugin> getPlugins();
}
