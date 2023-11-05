package org.aksw.jena_sparql_api.algebra.transform;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.op.OpGroup;

/** A transformer that adds SERVICE <cache:env://REMOTE> { } blocks */
public class TransformCacheGroupBy
    extends TransformCopy
{
    @Override
    public Op transform(OpGroup opGroup, Op subOp) {
        return super.transform(opGroup, subOp);
    }
}
