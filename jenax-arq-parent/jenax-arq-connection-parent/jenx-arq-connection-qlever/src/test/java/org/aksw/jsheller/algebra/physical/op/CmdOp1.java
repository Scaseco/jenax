package org.aksw.jsheller.algebra.physical.op;

import java.util.Objects;

public abstract class CmdOp1
    extends CmdOpBase
{
    protected CmdOp subOp;

    public CmdOp1(CmdOp subOp) {
        super();
        this.subOp = Objects.requireNonNull(subOp);
    }

    public CmdOp getSubOp() {
        return subOp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subOp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CmdOp1 other = (CmdOp1) obj;
        return Objects.equals(subOp, other.subOp);
    }
}
