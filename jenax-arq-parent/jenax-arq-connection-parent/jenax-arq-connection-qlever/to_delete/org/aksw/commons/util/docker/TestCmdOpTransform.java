package org.aksw.commons.util.docker;

import org.aksw.shellgebra.algebra.cmd.arg.CmdArg;
import org.aksw.shellgebra.algebra.cmd.arg.Token;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenPath;
import org.aksw.shellgebra.algebra.cmd.arg.Token.TokenVar;
import org.aksw.shellgebra.algebra.cmd.op.CmdOp;
import org.aksw.shellgebra.algebra.cmd.op.CmdOpExec;
import org.aksw.shellgebra.algebra.cmd.transformer.CmdOpTransformer;
import org.aksw.shellgebra.algebra.cmd.transformer.TokenTransform;
import org.aksw.shellgebra.algebra.cmd.transformer.TokenTransformBase;
import org.junit.Test;

public class TestCmdOpTransform {
    @Test
    public void testTransformFile() {
        CmdOp cmdOp = new CmdOpExec("/test", CmdArg.ofLiteral("-i"), CmdArg.ofPathString("/bar"));
        TokenTransform tokenTransform = new TokenTransformBase() {
            @Override
            public Token transform(TokenPath token) {
                return new TokenPath("/foo" + token.path());
            }
        };
        CmdOp newCmdOp = CmdOpTransformer.transform(cmdOp, null, null, tokenTransform);
        // TODO Validate that TokenPath is '/foo/bar'
        System.out.println(newCmdOp);
    }

    @Test
    public void testTransformVar() {
        CmdOp cmdOp = new CmdOpExec("/test", CmdArg.ofLiteral("-i"), CmdArg.ofPathString("/foo"), CmdArg.ofVarName("myVar"));
        TokenTransform tokenTransform = new TokenTransformBase() {
            @Override
            public Token transform(TokenVar token) {
                return new TokenPath("/foo/" + token.name());
            }
        };
        CmdOp newCmdOp = CmdOpTransformer.transform(cmdOp, null, null, tokenTransform);
        // TODO Validate that path is /foo/myVar.
        System.out.println(newCmdOp);
    }

}
