package org.aksw.jsheller.algebra.physical;

import java.util.List;

public interface CmdStrOps {
    String subst(String str);
    String quoteArg(String str);
    String group(List<String> strs);
    String pipe(String before, String after);
    String call(String cmdName, List<String> args);
}
