package org.aksw.jsheller.algebra.logical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.jsheller.algebra.physical.CmdOp;
import org.aksw.jsheller.algebra.physical.CmdOpExec;
import org.aksw.jsheller.algebra.physical.CmdOpFile;
import org.aksw.jsheller.algebra.physical.CmdOpGroup;
import org.aksw.jsheller.algebra.physical.CmdOpString;
import org.aksw.jsheller.algebra.physical.CmdOpSubst;

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
        // return op;
        // return new CodecOpCommand(CmdOpExec.of("cat", op.getPath()));
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
            // cmd[0] = resolvedCmdName;
            List<CmdOp> tmp = new ArrayList<>();
            Arrays.asList(cmd)
                .subList(1, cmd.length)
                .forEach(s -> tmp.add(new CmdOpString(s)));
            SysRuntime runtime = env.getRuntime();

            boolean canSubst = true;

            if (canSubst) {
                if (subOp instanceof CodecOpCommand subCmd) {
                    CmdOp cmdOp = subCmd.getCmdOp();

                    boolean isFileSupported = true;

                    CmdOp subC;
                    if (isFileSupported && cmdOp instanceof CmdOpFile fileOp) {
                        subC = new CmdOpFile(fileOp.getPath());
                    } else {
                    // String[] parts = runtime.compileCommand(cmdOp);
                    // CmdOp subC = new CmdOpSubst(CmdOpExec.of(parts));
                        subC = new CmdOpSubst(cmdOp);
                    }
                    // String subC = runtime.processSubstitute(subCmd.getCmdArray());
        //            String subC = switch (mode) {
        //                case PROCESS_SUBSTITUTE -> runtime.processSubstitute(subCmd.getCmdArray());
        //                case COMMAND_GROUP -> runtime.commandGroup(subCmd.getCmdArray());
        //            };

                    tmp.add(subC);

                    CmdOp newCmdOp = new CmdOpExec(resolvedCmdName, tmp);
                    result = new CodecOpCommand(newCmdOp);

                } if (subOp instanceof CodecOpFile cmdOfFile) {
                    result = new CodecOpCommand(CmdOpExec.ofStrings("cat", cmdOfFile.getPath()));
                }

            }
            // result = new CodecOpCommand(tmp.toArray(new String[0]));
        }

        // If filename: check if the codec can stream it, otherwise 'cat' it.
//        if (subOp instanceof CodecOpFile subCmd) {
//
//        } else if (subOp instanceof CodecOpCommand subCmd) {
//        }

        if (result == null) {
            result = super.transform(op, subOp);
        }
        return result;
    }

    /*
    @Override
    public CodecOp transform(CodecOpCommandGroup op, List<CodecOp> subOps) {
        boolean isAllCmds = subOps.stream().allMatch(subOp -> subOp instanceof CodecOpCommand);
        CodecOp result;
        if (isAllCmds) {
            List<String> parts = subOps.stream()
                .map(subOp -> (CodecOpCommand)subOp)
                .map(CodecOpCommand::getCmdArray)
                .map(SysRuntimeImpl::join)
                .toList(); // .toArray(String[]::new);
            String groupCmd = "{ " + parts.stream().collect(Collectors.joining(" ; ")) + " }";
            result = new CodecOpCommand(groupCmd);
        } else {
            result = super.transform(op, subOps);
        }
        return result;
    }

    @Override
    public CodecOp transform(CodecOpPipe op, CodecOp subOp1, CodecOp subOp2) {
        CodecOp result;
        if (subOp1 instanceof CodecOpCommand o1 && subOp2 instanceof CodecOpCommand o2) {
            String str = SysRuntimeImpl.join(o1.getCmdArray()) + " | " + SysRuntimeImpl.join(o2.getCmdArray());
            result = new CodecOpCommand(new String[] { str });
        } else {
            result = super.transform(op, subOp1, subOp2);
        }
        return result;
    }
    */
}
