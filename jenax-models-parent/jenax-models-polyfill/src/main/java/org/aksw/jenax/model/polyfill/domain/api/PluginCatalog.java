package org.aksw.jenax.model.polyfill.domain.api;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface PluginCatalog
    extends Resource
{
    @Iri(PolyfillTerms.plugins)
    Set<Plugin> getPlugins();
}
