package org.aksw.jsheller.algebra.logical.transform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.jsheller.algebra.logical.op.CodecOp;
import org.aksw.jsheller.algebra.logical.op.CodecOpCodecName;
import org.aksw.jsheller.algebra.logical.op.CodecOpCommand;
import org.aksw.jsheller.algebra.logical.op.CodecOpConcat;
import org.aksw.jsheller.algebra.logical.op.CodecOpFile;
import org.aksw.jsheller.algebra.logical.op.CodecOpTransformBase;
import org.aksw.jsheller.algebra.logical.op.CodecSpec;
import org.aksw.jsheller.algebra.logical.op.CodecSysEnv;
import org.aksw.jsheller.algebra.physical.op.CmdOp;
import org.aksw.jsheller.algebra.physical.op.CmdOpExec;
import org.aksw.jsheller.algebra.physical.op.CmdOpFile;
import org.aksw.jsheller.algebra.physical.op.CmdOpGroup;
import org.aksw.jsheller.algebra.physical.op.CmdOpPipe;
import org.aksw.jsheller.algebra.physical.op.CmdOpString;
import org.aksw.jsheller.algebra.physical.op.CmdOpSubst;
import org.aksw.jsheller.exec.SysRuntime;
import org.aksw.jsheller.registry.CodecRegistry;
import org.aksw.jsheller.registry.CodecVariant;

public class CodecTransformToCmdOp
    extends CodecOpTransformBase
{
    private CodecRegistry registry;
    private CodecSysEnv env;

    public CodecTransformToCmdOp(CodecRegistry registry, CodecSysEnv env) {
        super();
        this.registry = registry;
        this.env = env;
    }

    @Override
    public CodecOp transform(CodecOpFile op) {
        return new CodecOpCommand(new CmdOpFile(op.getPath()));
     }

    @Override
    public CodecOp transform(CodecOpConcat op, List<CodecOp> subOps) {
        boolean isPushable = subOps.stream().allMatch(x -> x instanceof CodecOpCommand);

        CodecOp result;
        if (isPushable) {
            List<CmdOp> args = subOps.stream().map(x -> (CodecOpCommand)x).map(CodecOpCommand::getCmdOp).toList();
            result = new CodecOpCommand(new CmdOpGroup(args));
        } else {
            result = super.transform(op, subOps);
        }
        return result;
    }

    @Override
    public CodecOp transform(CodecOpCodecName op, CodecOp subOp) {
        String name = op.getName();
        CodecOp result = null;

        CodecSpec spec = registry.requireCodec(name);
        for (CodecVariant variant : spec.getVariants()) {
            String[] cmd = variant.getCmd();
            if (cmd.length == 0) {
                throw new IllegalStateException("Encountered zero-length command");
            }
            String rawCmdName = cmd[0];
            String resolvedCmdName;
            try {
                resolvedCmdName = env.getRuntime().which(rawCmdName);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (resolvedCmdName == null) {
                continue;
            }

            // cmd[0] = resolvedCmdName;
            List<CmdOp> args = new ArrayList<>();
            Arrays.asList(cmd)
                .subList(1, cmd.length)
                .forEach(s -> args.add(new CmdOpString(s)));
            SysRuntime runtime = env.getRuntime();

            boolean canSubst = true;
            boolean supportsStdIn = true;
            boolean supportsFile = true;

            if (subOp instanceof CodecOpCommand subCmd) {
                CmdOp newCmdOp;
                CmdOp cmdOp = subCmd.getCmdOp();

                if (supportsFile && cmdOp instanceof CmdOpFile fileOp) {
                    args.add(new CmdOpFile(fileOp.getPath()));
                    newCmdOp = new CmdOpExec(resolvedCmdName, args);
                } else if (supportsStdIn) {
                    newCmdOp = new CmdOpExec(resolvedCmdName, args);
                    newCmdOp = new CmdOpPipe(cmdOp, newCmdOp);
                } else {
                    // String[] parts = runtime.compileCommand(cmdOp);
                    // CmdOp subC = new CmdOpSubst(CmdOpExec.of(parts));
                    args.add(new CmdOpSubst(cmdOp));
                    newCmdOp = new CmdOpExec(resolvedCmdName, args);
                }
                result = new CodecOpCommand(newCmdOp);
            } if (subOp instanceof CodecOpFile cmdOfFile) {
                result = new CodecOpCommand(CmdOpExec.ofStrings("cat", cmdOfFile.getPath()));
            }

            // Accept the first result
            if (result != null) {
                break;
            }
        }

        if (result == null) {
            result = super.transform(op, subOp);
        }
        return result;
    }
}
