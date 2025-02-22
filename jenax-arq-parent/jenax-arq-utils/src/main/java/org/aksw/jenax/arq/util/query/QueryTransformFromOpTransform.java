package org.aksw.jenax.arq.util.query;

import java.util.Objects;

import org.aksw.jenax.arq.util.op.OpTransform;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.apache.jena.query.Query;

public class QueryTransformFromOpTransform
    implements QueryTransform
{
    protected OpTransform transform;

    public QueryTransformFromOpTransform(OpTransform transform) {
        super();
        this.transform = Objects.requireNonNull(transform);
    }

    public OpTransform getOpTransform() {
        return transform;
    }

    @Override
    public Query apply(Query t) {
        Query result = QueryUtils.applyOpTransform(t, transform);
        return result;
    }

}
