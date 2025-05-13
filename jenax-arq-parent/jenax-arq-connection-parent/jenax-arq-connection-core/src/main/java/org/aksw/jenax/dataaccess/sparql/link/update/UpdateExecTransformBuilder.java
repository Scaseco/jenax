package org.aksw.jenax.dataaccess.sparql.link.update;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jenax.arq.util.exec.update.UpdateExecTransform;
import org.aksw.jenax.arq.util.query.TransformList;

public class UpdateExecTransformBuilder {
    protected List<UpdateExecTransform> execTransforms = new ArrayList<>();

    protected void addInternal(UpdateExecTransform item) {
        execTransforms.add(item);
    }

    public UpdateExecTransformBuilder add(UpdateExecTransform transform) {
        TransformList.streamFlatten(true, transform).forEach(this::addInternal);
        return this;
    }

    public UpdateExecTransform build() {
        return TransformList.flattenOrNull(true, UpdateExecTransformList::new, execTransforms.stream());
    }

    public void reset() {
        execTransforms.clear();
    }

    @Override
    public String toString() {
        return "UpdateExecTransformBuilder [execTransforms=" + execTransforms + "]";
    }
}
