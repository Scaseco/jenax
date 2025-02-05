package org.aksw.jsheller.algebra.physical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class CmdOpVisitorToString
    implements CmdOpVisitor<String>
{
    protected CmdStrOps strOps;

    public CmdOpVisitorToString(CmdStrOps strOps) {
        super();
        this.strOps = Objects.requireNonNull(strOps);
    }

    @Override
    public String visit(CmdOpExec op) {
        List<CmdOp> subOps = op.getSubOps();
        List<String> argStrs = transformAll(this, subOps);
        String result = strOps.call(op.getName(), argStrs);
        return result;
    }

    @Override
    public String visit(CmdOpPipe op) {
        String before = op.getSubOp1().accept(this);
        String after = op.getSubOp2().accept(this);
        String result = strOps.pipe(before, after);
        return result;
    }

    @Override
    public String visit(CmdOpGroup op) {
        List<String> strs = transformAll(this, op.getSubOps());
        String result = strOps.group(strs);
        return result;
    }

    @Override
    public String visit(CmdOpSubst op) {
        String str = op.getSubOp().accept(this);
        String result = strOps.subst(str);
        return result;
    }

    @Override
    public String visit(CmdOpString op) {
        return op.getValue();
    }

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

    @Override
    public String visit(CmdOpToArg op) {
        String str = op.getSubOp().accept(this);
        String result = strOps.quoteArg(str);
        return result;
    }

    /** For proper stringification file nodes of exec nodes need to be replaced with strings.
     *  See {@link CmdOpTransformArguments}
     */
    @Override
    public String visit(CmdOpFile op) {
        String str = op.getPath(); // op.getSubOp().accept(this);
        String result = strOps.quoteArg(str);
        return result;
    }
}
