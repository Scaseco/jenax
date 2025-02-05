package org.aksw.jsheller.algebra.physical.op;

import java.util.List;
import java.util.Objects;

public abstract class CmdOpN
    extends CmdOpBase
{
    protected List<CmdOp> subOps;

    public CmdOpN(List<CmdOp> subOps) {
        this.subOps = List.copyOf(subOps);
    }

    public List<CmdOp> getSubOps() {
        return subOps;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subOps);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CmdOpN other = (CmdOpN) obj;
        return Objects.equals(subOps, other.subOps);
    }
}
