package org.aksw.jenax.arq.util.query;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.arq.util.op.OpTransform;
import org.aksw.jenax.arq.util.op.OpTransformList;
import org.aksw.jenax.arq.util.op.OpTransforms;
import org.apache.jena.sparql.expr.ExprTransform;

public class OpTransformBuilder {
    protected List<OpTransform> transforms = new ArrayList<>();

    public OpTransformBuilder add(OpTransform transform) {
        TransformList.streamFlatten(true, transform).forEach(transforms::add);
        return this;
    }

    public OpTransformBuilder add(ExprTransform transform) {
        transforms.add(OpTransforms.of(transform));
        return this;
    }

    public OpTransform build() {
        return TransformList.flattenOrNull(true, OpTransformList::new, transforms.stream());
    }

    public void reset() {
        transforms.clear();
    }
}
