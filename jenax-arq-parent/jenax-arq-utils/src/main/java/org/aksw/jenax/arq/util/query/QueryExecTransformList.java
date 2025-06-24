package org.aksw.jenax.arq.util.query;

import java.util.List;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.apache.jena.sparql.exec.QueryExec;

public class QueryExecTransformList
    extends TransformList<QueryExec, QueryExecTransform>
    implements QueryExecTransform
{
    public QueryExecTransformList(List<QueryExecTransform> mods) {
        super(mods);
    }
}
