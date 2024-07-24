package org.aksw.jenax.io.json.graph;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.sparql.path.P_Path0;

public abstract class GraphToJsonNodeMapperObjectLike
    implements GraphToJsonNodeMapper
{
    protected Map<P_Path0, GraphToJsonEdgeMapper> propertyMappers = new LinkedHashMap<>();

    // @Override
    public Map<P_Path0, GraphToJsonEdgeMapper> getPropertyMappers() {
        return propertyMappers;
    }

    // Map<String, GraphToJsonEdgeMapper> getPropertyMappers();
}
