package org.aksw.jsheller.algebra.physical;

import java.util.Objects;

public abstract class CmdOp2
    extends CmdOpBase
{
    protected CmdOp subOp1;
    protected CmdOp subOp2;

    public CmdOp2(CmdOp subOp1, CmdOp subOp2) {
        super();
        this.subOp1 = Objects.requireNonNull(subOp1);
        this.subOp2 = Objects.requireNonNull(subOp2);
    }

    public CmdOp getSubOp1() {
        return subOp1;
    }

    public CmdOp getSubOp2() {
        return subOp2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subOp1, subOp2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CmdOp2 other = (CmdOp2) obj;
        return Objects.equals(subOp1, other.subOp1) && Objects.equals(subOp2, other.subOp2);
    }
}
