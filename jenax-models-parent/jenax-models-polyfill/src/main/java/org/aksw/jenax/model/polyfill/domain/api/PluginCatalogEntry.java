package org.aksw.jenax.model.polyfill.domain.api;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface PluginCatalogEntry
    extends Resource
{
    @Iri(PolyfillTerms.isEnabled)
    boolean isEnabled();
    PluginCatalogEntry setEnabled(boolean isEnabled);

    @Iri(PolyfillTerms.plugin)
    Plugin getPlugin();
    PluginCatalogEntry setPlugin(Resource plugin);
}
