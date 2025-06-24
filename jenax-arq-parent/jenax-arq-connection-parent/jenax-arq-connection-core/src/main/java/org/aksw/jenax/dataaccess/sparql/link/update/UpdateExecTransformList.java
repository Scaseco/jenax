package org.aksw.jenax.dataaccess.sparql.link.update;

import java.util.List;

import org.aksw.jenax.arq.util.exec.update.UpdateExecTransform;
import org.aksw.jenax.arq.util.query.TransformList;
import org.apache.jena.sparql.exec.UpdateExec;

public class UpdateExecTransformList
    extends TransformList<UpdateExec, UpdateExecTransform>
    implements UpdateExecTransform
{
    public UpdateExecTransformList(List<UpdateExecTransform> mods) {
        super(mods);
    }
}
