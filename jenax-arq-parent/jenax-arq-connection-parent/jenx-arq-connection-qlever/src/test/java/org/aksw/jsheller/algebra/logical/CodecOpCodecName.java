package org.aksw.jsheller.algebra.logical;

public class CodecOpCodecName
  extends CodecOp1
{
    protected String name;

    public CodecOpCodecName(String name, CodecOp subOp) {
        super(subOp);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(CodecOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "CodecOpCodecName [name=" + name + ", subOp=" + subOp + "]";
    }
}
