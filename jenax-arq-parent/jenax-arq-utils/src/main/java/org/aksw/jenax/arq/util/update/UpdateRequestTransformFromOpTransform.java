package org.aksw.jenax.arq.util.update;

import java.util.Objects;

import org.aksw.jenax.arq.util.op.OpTransform;
import org.apache.jena.update.UpdateRequest;

public class UpdateRequestTransformFromOpTransform
    implements UpdateRequestTransform
{
    protected OpTransform opTransform;

    public UpdateRequestTransformFromOpTransform(OpTransform opTransform) {
        super();
        this.opTransform = Objects.requireNonNull(opTransform);
    }

    public OpTransform getOpTransform() {
        return opTransform;
    }

    @Override
    public UpdateRequest apply(UpdateRequest t) {
        UpdateRequest result = UpdateRequestUtils.applyOpTransform(t, opTransform);
        return result;
    }
}
