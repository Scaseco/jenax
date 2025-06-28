package org.aksw.shellgebra.unused.algebra.plan;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpVar;
import org.aksw.shellgebra.algebra.stream.op.StreamOpCommand;
import org.aksw.shellgebra.algebra.stream.op.StreamOpConcat;
import org.aksw.shellgebra.algebra.stream.op.StreamOpContentConvert;
import org.aksw.shellgebra.algebra.stream.op.StreamOpFile;
import org.aksw.shellgebra.algebra.stream.op.StreamOpTranscode;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVar;
import org.aksw.shellgebra.algebra.stream.op.StreamOpVisitor;
import org.aksw.shellgebra.docker.adapter.cli.ContainerDef;
import org.aksw.shellgebra.exec.stage.FileWriterTask;
import org.aksw.shellgebra.registry.codec.CodecRegistry;

import com.github.dockerjava.api.model.Bind;

// Use ContainerDef or use a custom class?
//class DockerInvocation {
//    protected String imageName;
//    protected String entryPoint;
//    protected String user;
//    protected Map<String, String> mountMap;
//}

class PlanNode {
    ContainerDef containerDef;
    CmdOp cmdOp;
}

class BoundCommand {
  CmdOp cmdOp; // the command op; non-stream inputs must be mapped to named pipes
  Map<String, FileWriterTask> fileTask; // Mapping of file names in the command to file writer tasks if they are named pipes.
                              // This is implicitly a mount map - but note, that the container path is first and the host path second.
}


class Env {
    protected String imageName; // might refer to host
    protected CmdOp cmdOp; // physical command, because the logical operation must have been resolved against the environment.

    protected Map<CmdOpVar, Env> subEnvs;

    /**
     * File writers that need to be started before launching the the command.
     * Path refers to the host path.
     */
    protected Map<Path, FileWriterTask> fileWriters;

    /** Host path to container path. */
    protected List<Bind> mountMap;
}

/** Class to inspect environments. */
class EnvContext {

}

//
//public class TransformStreamOpToPlan
//    implements StreamOpVisitor<PlanNode>
//{
//    protected List<Env> envs;
//    protected CodecRegistry codecRegistry;
//
//    @Override
//    public PlanNode visit(StreamOpFile op) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public PlanNode visit(StreamOpTranscode op) {
//        String codecName = op.getName();
//        // CodecSpec codecSpec = codecRegistry.getCodecSpec(codecName);
//
//
//        return null;
//    }
//
//    @Override
//    public PlanNode visit(StreamOpContentConvert op) {
//        // Check the current environment for whether it can handle the conversion.
//
//
//        op.getSourceFormat();
//        op.getTargetFormat();
//        op.getSubOp();
//
//
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public PlanNode visit(StreamOpConcat op) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public PlanNode visit(StreamOpCommand op) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    public PlanNode visit(StreamOpVar op) {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//
//}
