package jenax.engine.qlever.cmd.model;

import org.aksw.jsheller.algebra.cmd.op.CmdOp;

public class CallBuilderCodecBase {
    protected String[] baseCmd;

    public CmdOp build(CallSpec callSpec) {
        CmdOp inputSource = callSpec.inputSource();

        // PhysicalOpExec execOp = new PhysicalOpExec();

//    	if (inputSource != null) {
//    		result = PhysicalOpPipe(inputSource, );
//    	}

        return null;
    }
}
