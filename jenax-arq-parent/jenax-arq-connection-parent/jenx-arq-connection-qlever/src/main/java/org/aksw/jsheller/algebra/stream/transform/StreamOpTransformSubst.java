package org.aksw.jsheller.algebra.stream.transform;

import java.util.Map;
import java.util.function.Function;

import org.aksw.jsheller.algebra.stream.op.StreamOp;
import org.aksw.jsheller.algebra.stream.op.StreamOpVar;
import org.aksw.jsheller.algebra.stream.transformer.StreamOpTransform;
import org.aksw.jsheller.algebra.stream.transformer.StreamOpTransformBase;
import org.aksw.jsheller.algebra.stream.transformer.StreamOpTransformer;

/** Resolves variables */
public class StreamOpTransformSubst
    extends StreamOpTransformBase
{
    protected Function<String, ? extends StreamOp> varNameResolver;
    protected boolean failIfUnresolvable;

    public StreamOpTransformSubst(Function<String, ? extends StreamOp> varNameResolver) {
        this(false, varNameResolver);
    }

    public StreamOpTransformSubst(boolean failIfUnresolvable, Function<String, ? extends StreamOp> varNameResolver) {
        super();
        this.varNameResolver = varNameResolver;
        this.failIfUnresolvable = failIfUnresolvable;
    }

    @Override
    public StreamOp transform(StreamOpVar op) {
        String varName = op.getVarName();
        StreamOp resolvedOp = varNameResolver.apply(varName);
        StreamOp result;
        if (resolvedOp == null) {
            if (failIfUnresolvable) {
                throw new RuntimeException("Could not resolve " + varName);
            }
            result = resolvedOp;
        } else {
            // Retain the OpVar
            result = op;
        }
        return result;
    }

    public static StreamOp subst(StreamOp op, Map<String, ? extends StreamOp> varNameMap) {
        return subst(op, varNameMap::get);
    }

    public static StreamOp subst(StreamOp op, Function<String, ? extends StreamOp> varNameResolver) {
        StreamOpTransform transform = new StreamOpTransformSubst(varNameResolver);
        StreamOp result = StreamOpTransformer.transform(op, transform);
        return result;
    }
}
