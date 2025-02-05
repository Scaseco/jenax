package org.aksw.jsheller.algebra.physical.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.aksw.jsheller.algebra.physical.op.CmdOp;
import org.aksw.jsheller.algebra.physical.op.CmdOpVisitor;

public class CmdOpTransformLib {

    public static <T> List<T> transformAll(CmdOpVisitor<T> visitor, List<? extends CmdOp> ops) {
        List<T> result = new ArrayList<>(ops.size());
        transformAll(result, visitor, ops);
        return result;
    }

    public static <T> void transformAll(Collection<T> accumulator, CmdOpVisitor<T> visitor, List<? extends CmdOp> ops) {
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(visitor);
        for (CmdOp op : ops) {
            T contrib = op.accept(visitor);
            accumulator.add(contrib);
        }
    }

    public static <T, U> List<U> transformAll(CmdOpVisitor<T> visitor, List<? extends CmdOp> ops, Function<? super T, ? extends U> mapper) {
        List<U> result = new ArrayList<>(ops.size());
        transformAll(result, visitor, ops, mapper);
        return result;
    }

    public static <T, U> void transformAll(Collection<U> accumulator, CmdOpVisitor<T> visitor, List<? extends CmdOp> ops, Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(visitor);
        for (CmdOp op : ops) {
            T contrib = op.accept(visitor);
            U item = mapper.apply(contrib);
            accumulator.add(item);
        }
    }
}
