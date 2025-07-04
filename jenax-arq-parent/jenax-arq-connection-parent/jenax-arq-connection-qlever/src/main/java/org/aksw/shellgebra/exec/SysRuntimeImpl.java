package org.aksw.shellgebra.exec;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jenax.engine.qlever.SystemUtils;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVisitor;
import org.aksw.shellgebra.algebra.cmd.transform.CmdOpTransformArguments;
import org.aksw.shellgebra.algebra.cmd.transform.CmdOpVisitorToCmdString;
import org.aksw.shellgebra.algebra.cmd.transform.CmdString;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SysRuntimeImpl
    implements SysRuntime
{
    private static final Logger logger = LoggerFactory.getLogger(SysRuntimeImpl.class);

    protected CmdStrOps strOps;
    protected CmdOpVisitor<CmdString> stringifier;

    public static SysRuntime forBash() {
        return new SysRuntimeImpl(new CmdStrOpsBash());
    }

    public static SysRuntime forCurrentOs() {
        // FIXME Hack! Implement proper OS probing and configure the runtime appropriately.
        return forBash();
    }

    public SysRuntimeImpl(CmdStrOps strOps) {
        Objects.requireNonNull(strOps);
        this.strOps = strOps;
        this.stringifier = new CmdOpVisitorToCmdString(strOps);
    }

    @Override
    public CmdStrOps getStrOps() {
        return strOps;
    }

//    public SysRuntimeImpl(CmdOpVisitor<String> stringifier) {
//        super();
//    }

    protected String getBashPath() {
        String result;
        try {
            result = which("bash");
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve bash");
        }
        return result;
    }

    @Override
    public String which(String cmdName) throws IOException {
        return SystemUtils.which(cmdName);
    }

    @Override
    public String[] compileCommand(CmdOp op) {
        // Transform file arguments to properly quoted strings
        CmdOp tmpOp = CmdOpTransformer.transform(op, new CmdOpTransformArguments(this));
        CmdString cmdStr = tmpOp.accept(stringifier);
        String[] result;
        if (cmdStr.isScriptString()) {
            String bashPath = getBashPath();
            String scriptString = cmdStr.scriptString();

//            String sanitized = scriptString
//                    .replaceAll("\\{", "\\\\{")
//                    .replaceAll("\\}", "\\\\}")
//                    ;

            // Note that scriptString does not need quotes!
            result = new String[] { bashPath, "-c", scriptString };
        } else {
            String[] x = cmdStr.cmd();
            result = Arrays.copyOf(x, x.length);
        }
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

    @Override
    public void createNamedPipe(Path path) throws IOException {
        String absPathStr = path.toAbsolutePath().toString();
        String resolvedCmd = which("mkfifo");
        SystemUtils.runAndWait(logger::info, resolvedCmd, absPathStr);
    }
}
