package org.aksw.jsheller.algebra.cmd.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.jsheller.algebra.cmd.op.CmdOp;
import org.aksw.jsheller.algebra.cmd.op.CmdOpExec;
import org.aksw.jsheller.algebra.cmd.op.CmdOpFile;
import org.aksw.jsheller.algebra.cmd.op.CmdOpGroup;
import org.aksw.jsheller.algebra.cmd.op.CmdOpPipe;
import org.aksw.jsheller.algebra.cmd.op.CmdOpRedirect;
import org.aksw.jsheller.algebra.cmd.op.CmdOpString;
import org.aksw.jsheller.algebra.cmd.op.CmdOpSubst;
import org.aksw.jsheller.algebra.cmd.op.CmdOpToArg;
import org.aksw.jsheller.algebra.cmd.op.CmdOpVisitor;
import org.aksw.jsheller.exec.CmdStrOps;

// Note: CmdString does not ensure that the string is actually a command -
// e.g. when passing a CmdOpFile it will return a string with the file name
// So if the result is expected to be an executable operation, then the check needs
// to be made beforehand by examining the type of the CmdOp.
public class CmdOpVisitorToCmdString
    implements CmdOpVisitor<CmdString>
{
    protected CmdStrOps strOps;

    public String toArg(CmdString str) {
        String result = str.isScriptString()
            ? str.scriptString()
            : toArg(str.cmd()); // String.join(" ", str.cmd());
        return result;
    }

    /**
     * Convert a command array (including command name) into a script string.
     * The script string can be passed as an argument to the appropriate interpreted such
     * as [/bin/bash, -c, scriptString].
     * All arguments will be quoted as needed.
     */
    public String toArg(String[] cmd) {
        String result = Stream.concat(
            Stream.of(cmd[0]),
            Arrays.asList(cmd).subList(1, cmd.length).stream().map(strOps::quoteArg)
        ).collect(Collectors.joining(" "));
        return result;
    }

    public CmdOpVisitorToCmdString(CmdStrOps strOps) {
        super();
        this.strOps = Objects.requireNonNull(strOps);
    }

    @Override
    public CmdString visit(CmdOpExec op) {
        List<CmdOp> subOps = op.getSubOps();
        List<String> argStrs = new ArrayList<>(1 + subOps.size());
        argStrs.add(op.getName());
        CmdOpTransformLib.transformAll(argStrs, this, subOps, this::toArg);
        CmdString result = new CmdString(argStrs.toArray(String[]::new));// strOps.call(op.getName(), argStrs);
        return result;
    }

    @Override
    public CmdString visit(CmdOpPipe op) {
        String before = toArg(op.getSubOp1().accept(this));
        String after = toArg(op.getSubOp2().accept(this));
        CmdString result = new CmdString(strOps.pipe(before, after));
        return result;
    }

    @Override
    public CmdString visit(CmdOpGroup op) {
        List<String> strs = CmdOpTransformLib.transformAll(this, op.getSubOps(), this::toArg);
        CmdString result = new CmdString(strOps.group(strs));
        return result;
    }

    @Override
    public CmdString visit(CmdOpSubst op) {
        String str = toArg(op.getSubOp().accept(this));
        CmdString result = new CmdString(strOps.subst(str));
        return result;
    }

    @Override
    public CmdString visit(CmdOpString op) {
        return new CmdString(op.getValue());
    }

    @Override
    public CmdString visit(CmdOpToArg op) {
        String str = toArg(op.getSubOp().accept(this));
        String result = strOps.quoteArg(str);
        return new CmdString(result);
    }

    /** For proper stringification file nodes of exec nodes need to be replaced with strings.
     *  See {@link CmdOpTransformArguments}
     */
    @Override
    public CmdString visit(CmdOpFile op) {
        String str = op.getPath(); // op.getSubOp().accept(this);
        CmdString result = new CmdString(strOps.quoteArg(str));
        return result;
    }

    @Override
    public CmdString visit(CmdOpRedirect op) {
        String before = toArg(op.getSubOp().accept(this));
        String fileName = op.getFileName();
        CmdString result = new CmdString(strOps.redirect(before, fileName));
        return result;
    }
}
