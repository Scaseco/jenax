package org.aksw.jsheller.algebra.logical;

import org.aksw.jsheller.algebra.physical.CmdOp;

public class CodecOpCommand
    extends CodecOp0
{
    protected CmdOp cmdOp;
//    protected String[] cmd;
//    protected List<String> cmdView;

    public CodecOpCommand(CmdOp cmdOp) {
        super();
        this.cmdOp = cmdOp;
//        this.cmd = Arrays.copyOf(cmd, cmd.length);
//        this.cmdView = Collections.unmodifiableList(Arrays.asList(cmd));
    }

    public CmdOp getCmdOp() {
        return cmdOp;
    }

//    public List<String> getCmd() {
//        return cmdView;
//    }
//
//    public String[] getCmdArray() {
//        return Arrays.copyOf(cmd, cmd.length);
//    }

    @Override
    public <T> T accept(CodecOpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    @Override
    public String toString() {
        return "CodecOpCommand [cmd=" + cmdOp + "]";
    }
}
