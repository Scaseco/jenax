package org.aksw.jenax.model.polyfill.domain.api;

import java.util.Map;
import java.util.Set;

import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface PolyfillConfigurationSet
    extends Resource
{
    Set<PolyfillConfiguration> getConfigurations();
    Map<String, PolyfillConfiguration> getConfigurationsByProfileName();
}
