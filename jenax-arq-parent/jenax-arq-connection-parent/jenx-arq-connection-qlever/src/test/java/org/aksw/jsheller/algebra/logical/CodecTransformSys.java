package org.aksw.jsheller.algebra.logical;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.jsheller.algebra.physical.CmdOpExec;

//public class CodecTransformSys
//    extends CodecOpTransformBase
//{
////    public static enum Mode {
////        /** cmd1 | cmd2 */
////        PIPE,
////
////        /** &lt;(cmd1) ... &lt;(cmdN) */
////        PROCESS_SUBSTITUTE,
////
////        /** { cmd1 ; ... ; cmdN } */
////        COMMAND_GROUP
////    }
//
//    private CodecRegistry registry;
//    private CodecSysEnv env;
//    // private Mode mode;
//
//    public CodecTransformSys(CodecRegistry registry, CodecSysEnv env) {
//        super();
//        this.registry = registry;
//        this.env = env;
//        // this.mode = Objects.requireNonNull(mode);
//    }
//
////    public static CodecTransformSys of(CodecRegistry registry, CodecSysEnv env) {
////        return new CodecTransformSys(registry, env);
////    }
//
//    @Override
//    public CodecOp transform(CodecOpFile op) {
//        return new CodecOpCommand(CmdOpExec.of("cat", op.getPath()));
//    }
//
//    @Override
//    public CodecOp transform(CodecOpCodecName op, CodecOp subOp) {
//        String name = op.getName();
//        CodecOp result = null;
//
//        if (subOp instanceof CodecOpCommand subCmd) {
//            CodecSpec spec = registry.requireCodec(name);
//            for (CodecVariant variant : spec.getVariants()) {
//                String[] cmd = variant.getCmd();
//                if (cmd.length == 0) {
//                    throw new IllegalStateException("Encountered zero-length command");
//                }
//                String c = cmd[0];
//                String resolved;
//                try {
//                    resolved = env.getRuntime().which(c);
//                } catch (IOException | InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                cmd[0] = resolved;
//                List<String> tmp = new ArrayList<>(Arrays.asList(cmd));
//                SysRuntime runtime = env.getRuntime();
//
//                String subC = runtime.processSubstitute(subCmd.getCmdArray());
////                String subC = switch (mode) {
////                    case PROCESS_SUBSTITUTE -> runtime.processSubstitute(subCmd.getCmdArray());
////                    case COMMAND_GROUP -> runtime.commandGroup(subCmd.getCmdArray());
////                };
//
//                tmp.add(subC);
//
//                result = new CodecOpCommand(tmp.toArray(new String[0]));
//            }
//        }
//
//        if (result == null) {
//            result = super.transform(op, subOp);
//        }
//        return result;
//    }
//
//    /*
//    @Override
//    public CodecOp transform(CodecOpCommandGroup op, List<CodecOp> subOps) {
//        boolean isAllCmds = subOps.stream().allMatch(subOp -> subOp instanceof CodecOpCommand);
//        CodecOp result;
//        if (isAllCmds) {
//            List<String> parts = subOps.stream()
//                .map(subOp -> (CodecOpCommand)subOp)
//                .map(CodecOpCommand::getCmdArray)
//                .map(SysRuntimeImpl::join)
//                .toList(); // .toArray(String[]::new);
//            String groupCmd = "{ " + parts.stream().collect(Collectors.joining(" ; ")) + " }";
//            result = new CodecOpCommand(groupCmd);
//        } else {
//            result = super.transform(op, subOps);
//        }
//        return result;
//    }
//
//    @Override
//    public CodecOp transform(CodecOpPipe op, CodecOp subOp1, CodecOp subOp2) {
//        CodecOp result;
//        if (subOp1 instanceof CodecOpCommand o1 && subOp2 instanceof CodecOpCommand o2) {
//            String str = SysRuntimeImpl.join(o1.getCmdArray()) + " | " + SysRuntimeImpl.join(o2.getCmdArray());
//            result = new CodecOpCommand(new String[] { str });
//        } else {
//            result = super.transform(op, subOp1, subOp2);
//        }
//        return result;
//    }
//    */
//}
