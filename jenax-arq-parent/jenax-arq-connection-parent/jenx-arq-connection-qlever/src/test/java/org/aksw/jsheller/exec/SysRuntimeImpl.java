package org.aksw.jsheller.exec;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jsheller.algebra.physical.op.CmdOp;
import org.aksw.jsheller.algebra.physical.op.CmdOpTransformer;
import org.aksw.jsheller.algebra.physical.op.CmdOpVisitor;
import org.aksw.jsheller.algebra.physical.transform.CmdOpTransformArguments;
import org.aksw.jsheller.algebra.physical.transform.CmdOpVisitorToString;

import jenax.engine.qlever.SystemUtils;

public class SysRuntimeImpl
    implements SysRuntime
{
    protected CmdStrOps strOps;
    protected CmdOpVisitor<String> stringifier;

    public static SysRuntime forBash() {
        return new SysRuntimeImpl(new CmdStrOpsBash());
    }

    public SysRuntimeImpl(CmdStrOps strOps) {
        Objects.requireNonNull(strOps);
        this.strOps = strOps;
        this.stringifier = new CmdOpVisitorToString(strOps);
    }

//    public SysRuntimeImpl(CmdOpVisitor<String> stringifier) {
//        super();
//    }

    @Override
    public String which(String cmdName) throws IOException, InterruptedException {
        return SystemUtils.which(cmdName);
    }

    @Override
    public String[] compileCommand(CmdOp op) {
        // Transform file arguments to properly quoted strings
        CmdOp tmp = CmdOpTransformer.transform(op, new CmdOpTransformArguments(this));

        // Hack - distinguish between indirect and direct commands
        String[] result = new String[] { tmp.accept(stringifier) };
        return result;
    }

//    @Override
//    public String processSubstitute(String... cmd) {
//        return "<(" + join(cmd) + ")";
//    }
//
//    @Override
//    public String commandGroup(List<String[]> cmds) {
//        return "{ " + cmds.stream().map(SysRuntimeImpl::join).collect(Collectors.joining(" ; ")) + " }";
//    }

    public static String join(String ...cmd) {
        return Stream.of(cmd).collect(Collectors.joining(" "));
    }

    public static String quoteArg(String cmd) {
        return cmd.contains(" ")
            ? "'" + cmd.replaceAll("'", "\\'") + "'"
            : cmd;
    }

    @Override
    public String quoteFileArgument(String fileName) {
        return strOps.quoteArg(fileName);
    }
}
