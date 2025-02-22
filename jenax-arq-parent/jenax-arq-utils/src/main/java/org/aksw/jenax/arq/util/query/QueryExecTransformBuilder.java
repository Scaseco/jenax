package org.aksw.jenax.arq.util.query;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;

public class QueryExecTransformBuilder {
    protected List<QueryExecTransform> execTransforms = new ArrayList<>();

    protected void addInternal(QueryExecTransform item) {
        execTransforms.add(item);
    }

    public QueryExecTransformBuilder add(QueryExecTransform transform) {
        TransformList.streamFlatten(true, transform).forEach(this::addInternal);
        return this;
    }

    public QueryExecTransform build() {
        return TransformList.flattenOrNull(true, QueryExecTransformList::new, execTransforms.stream());
    }

    public void reset() {
        execTransforms.clear();
    }
}
