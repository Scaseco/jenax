package org.aksw.shellgebra.exec;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdStrOpsBash
    implements CmdStrOps
{
    @Override
    public String subst(String str) {
        return "<(" + str + ")";
    }

    @Override
    public String quoteArg(String cmd) {
        String result = cmd;

//        result = result
//            .replaceAll("\\{", "\\\\{")
//            .replaceAll("\\|", "\\\\|")
//            .replaceAll("\\}", "\\\\}");

        boolean containsSingleQuote = result.contains("'");
        if (containsSingleQuote) {
            // Escape single quotes
            result = "'" + result.replaceAll("\\'", "'\"'\"'") + "'"; // ' -> '"'"'
        } else {
            // Escape any white spaces
            result = result.replaceAll(" ", "\\\\ ");
        }

        return result;
    }

    @Override
    public String group(List<String> strs) {
        // String result = "{ " + strs.stream().collect(Collectors.joining(" ; ")) + " }";
        String result = "{ " + strs.stream().map(x -> x + " ; ").collect(Collectors.joining()) + "}";
        return result;
    }

    @Override
    public String pipe(String before, String after) {
        return before + " | " + after;
    }

    @Override
    public String call(String cmdName, List<String> args) {
        String result = Stream.concat(
                Stream.of(cmdName),
                args.stream())
            .collect(Collectors.joining(" "));
        return result;
    }

    @Override
    public String redirect(String cmdStr, String fileName) {
        return cmdStr + " > " + fileName;
    }
}
