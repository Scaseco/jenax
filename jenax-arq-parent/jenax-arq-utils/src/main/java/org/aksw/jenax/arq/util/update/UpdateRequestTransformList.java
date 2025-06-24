package org.aksw.jenax.arq.util.update;

import java.util.List;

import org.aksw.jenax.arq.util.query.TransformList;
import org.apache.jena.update.UpdateRequest;

public class UpdateRequestTransformList
    extends TransformList<UpdateRequest, UpdateRequestTransform>
    implements UpdateRequestTransform
{
    public UpdateRequestTransformList(List<UpdateRequestTransform> mods) {
        super(mods);
    }
}
