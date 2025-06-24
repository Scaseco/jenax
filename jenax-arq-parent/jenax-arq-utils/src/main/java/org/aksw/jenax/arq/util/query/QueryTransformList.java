package org.aksw.jenax.arq.util.query;

import java.util.List;

import org.apache.jena.query.Query;

public class QueryTransformList
    extends TransformList<Query, QueryTransform>
    implements QueryTransform
{
    public QueryTransformList(List<QueryTransform> mods) {
        super(mods);
    }
}
