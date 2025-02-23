package org.aksw.jenax.arq.util.update;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.op.OpTransform;
import org.aksw.jenax.arq.util.query.OpTransformBuilder;
import org.aksw.jenax.arq.util.query.TransformList;
import org.apache.jena.sparql.expr.ExprTransform;

public class UpdateRequestTransformBuilder {
    protected List<UpdateRequestTransform> uTransforms = new ArrayList<>();
    protected OpTransformBuilder opTransformBuilder = new OpTransformBuilder();

    protected UpdateRequestTransform pendingTransform() {
        UpdateRequestTransform result = null;
        OpTransform opTransform = opTransformBuilder.build();
        if (opTransform != null) {
            result = new UpdateRequestTransformFromOpTransform(opTransform);
        }
        return result;
    }

    protected void finalizeSubBuilder() {
        UpdateRequestTransform contrib = pendingTransform();
        if (contrib != null) {
            uTransforms.add(contrib);
            opTransformBuilder.reset();
        }
    }

    protected void addInternal(UpdateRequestTransform transform) {
        if (transform instanceof UpdateRequestTransformFromOpTransform t) {
            OpTransform rewrite = t.getOpTransform();
            opTransformBuilder.add(rewrite);
        } else {
            finalizeSubBuilder();
            uTransforms.add(transform);
        }
    }

    public UpdateRequestTransformBuilder add(UpdateRequestTransform transform) {
        TransformList.streamFlatten(true, transform).forEach(this::addInternal);
        return this;
    }

    public UpdateRequestTransformBuilder add(OpTransform transform) {
        opTransformBuilder.add(transform);
        return this;
    }

    public UpdateRequestTransformBuilder add(ExprTransform transform) {
        opTransformBuilder.add(transform);
        return this;
    }

    public UpdateRequestTransform build() {
        Stream<UpdateRequestTransform> stream = uTransforms.stream();

        // Add an op transform if it is pending
        UpdateRequestTransform lastContrib = pendingTransform();
        if (lastContrib != null) {
            stream = Stream.concat(stream, Stream.of(lastContrib));
        }
        return TransformList.flattenOrNull(true, UpdateRequestTransformList::new, stream);
    }

    public void reset() {
        uTransforms.clear();
        opTransformBuilder.reset();
    }
}
