package org.aksw.jenax.model.polyfill.domain.api;

import org.apache.jena.rdf.model.Resource;

public interface PolyfillCondition
    extends Resource
{
    <T> T accept(PolyfillConditionVisitor<T> visitor);
}
