package jenax.engine.qlever.cmd.model;

import java.util.List;

import org.aksw.jsheller.algebra.cmd.op.CmdOp;

/** Specification for how to invoke a processor. */
public record CallSpec(CmdOp inputSource, List<CmdOp> inputs, String outputUrl) {}
