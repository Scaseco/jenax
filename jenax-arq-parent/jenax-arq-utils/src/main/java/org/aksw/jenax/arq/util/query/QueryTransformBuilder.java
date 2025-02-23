package org.aksw.jenax.arq.util.query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.op.OpTransform;
import org.apache.jena.sparql.expr.ExprTransform;

public class QueryTransformBuilder {
    protected List<QueryTransform> queryTransforms = new ArrayList<>();
    protected OpTransformBuilder opTransformBuilder = new OpTransformBuilder();

    protected void finalizeSubBuilder() {
        OpTransform opTransform = opTransformBuilder.build();
        if (opTransform != null) {
            queryTransforms.add(new QueryTransformFromOpTransform(opTransform));
            opTransformBuilder.reset();
        }
    }

    protected void addInternal(QueryTransform transform) {
        if (transform instanceof QueryTransformFromOpTransform t) {
            OpTransform rewrite = t.getOpTransform();
            opTransformBuilder.add(rewrite);
        } else {
            finalizeSubBuilder();
            queryTransforms.add(transform);
        }
    }

    public QueryTransformBuilder add(QueryTransform transform) {
        TransformList.streamFlatten(true, transform).forEach(this::addInternal);
        return this;
    }

    public QueryTransformBuilder add(OpTransform transform) {
        opTransformBuilder.add(transform);
        return this;
    }

    public QueryTransformBuilder add(ExprTransform transform) {
        opTransformBuilder.add(transform);
        return this;
    }

    public QueryTransform build() {
        Stream<QueryTransform> stream = queryTransforms.stream();

        // Add an op transform if it is pending
        OpTransform opTransform = opTransformBuilder.build();
        if (opTransform != null) {
            stream = Stream.concat(stream, Stream.of(new QueryTransformFromOpTransform(opTransform)));
        }
        return TransformList.flattenOrNull(true, QueryTransformList::new, stream);
    }

    public void reset() {
        queryTransforms.clear();
        opTransformBuilder.reset();
    }
}
