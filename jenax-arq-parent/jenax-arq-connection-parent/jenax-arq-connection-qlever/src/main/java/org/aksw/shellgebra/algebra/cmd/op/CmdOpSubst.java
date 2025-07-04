package org.aksw.shellgebra.algebra.cmd.op;

/**
 * Operator for bash process substitution.
 *
 * <p>
 * Example:
 * <pre>
 * bzip2 -dc <( input.bz2 )
 * </pre>
 *
 */
public class CmdOpSubst
    extends CmdOp1
{
    public CmdOpSubst(CmdOp subOp) {
        super(subOp);
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(subst " + getSubOp() + ")";
    }
}
