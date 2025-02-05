package org.aksw.jsheller.algebra.physical.op;

import java.util.Objects;

/**
 * CmdOpString instances are used to model constant command arguments and
 * should thus only appear as children of CmdOpExec nodes:
 * <pre>
 * (exec "/bin/bash" (string "-c") (string "echo 'hi'"))
 * </pre>
 */
public class CmdOpString
    extends CmdOp0
{
    protected String value;

    public CmdOpString(String value) {
        super();
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public <T> T accept(CmdOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CmdOpString other = (CmdOpString) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return "(str " + value + ")";
    }
}
