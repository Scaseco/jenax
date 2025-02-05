package jenax.engine.qlever.cmd.model;

import java.util.List;

import org.aksw.jsheller.algebra.physical.CmdOp;

public class CallSpecBuilder {
    protected String outputUrl;

    protected List<CmdOp> inputs;

    /** Stdin source */
    protected CmdOp inputSource;

    public List<CmdOp> getInputs() {
        return inputs;
    }

    public CmdOp getInputSource() {
        return inputSource;
    }

    public String getOutputUrl() {
        return outputUrl;
    }

    public CallSpec build() {
        return new CallSpec(inputSource, inputs, outputUrl);
    }
}
