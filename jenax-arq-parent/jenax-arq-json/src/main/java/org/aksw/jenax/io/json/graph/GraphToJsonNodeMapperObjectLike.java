package org.aksw.jenax.io.json.graph;

import java.util.Map;

public interface GraphToJsonNodeMapperObjectLike
    extends GraphToJsonNodeMapper
{
    Map<String, GraphToJsonPropertyMapper> getPropertyMappers();
}
