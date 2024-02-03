package org.aksw.jenax.model.polyfill.domain.api;

import java.util.Map;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.KeyIri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.annotation.reprogen.ValueIri;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface PluginConfiguration
    extends Resource
{
    Plugin getPlugin();
    PluginConfiguration setPlugin(Resource plugin);

    @Iri(PolyfillTerms.property)
    @KeyIri("http://www.example.org/key")
    @ValueIri("http://www.example.org/value")
    Map<String, Node> getProperties();
}
