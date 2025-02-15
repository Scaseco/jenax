package org.aksw.jsheller.exec;

import java.util.List;

public interface CmdStrOps {
    String subst(String str);
    String quoteArg(String str);
    String group(List<String> strs);
    String pipe(String before, String after);
    String call(String cmdName, List<String> args);
    String redirect(String cmd, String fileName);
    // exprEval $(...) -> Eval expression and substitute argument with result
}
