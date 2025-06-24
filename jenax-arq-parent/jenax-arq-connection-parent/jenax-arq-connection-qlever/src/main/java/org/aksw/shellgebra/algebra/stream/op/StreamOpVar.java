package org.aksw.shellgebra.algebra.stream.op;

public class StreamOpVar
    extends StreamOp0
{
    protected String varName;

    public StreamOpVar(String varName) {
        super();
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public <T> T accept(StreamOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "(var " + varName + ")";
    }
}
