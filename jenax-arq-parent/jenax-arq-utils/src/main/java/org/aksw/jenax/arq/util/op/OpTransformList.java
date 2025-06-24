package org.aksw.jenax.arq.util.op;

import java.util.List;

import org.aksw.jenax.arq.util.query.TransformList;
import org.apache.jena.sparql.algebra.Op;

public class OpTransformList
    extends TransformList<Op, OpTransform>
    implements OpTransform
{
    public OpTransformList(List<OpTransform> transforms) {
        super(transforms);
    }
}
