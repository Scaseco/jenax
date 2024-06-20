package org.aksw.jenax.io.json.graph;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class GraphToJsonNodeMapperObjectLike
    implements GraphToJsonNodeMapper
{
    protected Map<String, GraphToJsonEdgeMapper> propertyMappers = new LinkedHashMap<>();

    // @Override
    public Map<String, GraphToJsonEdgeMapper> getPropertyMappers() {
        return propertyMappers;
    }

    // Map<String, GraphToJsonEdgeMapper> getPropertyMappers();
}
