package org.aksw.jenax.stmt.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ComparisonChain;
import org.apache.jena.query.QueryParseException;

public class QueryParseExceptionUtils {
    public static final Pattern posPattern = Pattern.compile("(line )(\\d+)(, column )(\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * Replace 0 values in line and/or column with 1
     *
     * @param in
     * @return
     */
    public static int[] adjustLineAndCol(int[] in) {
        int[] result;
        if(in != null) {
            int x = in[0];
            int y = in[1];
            result = new int[] {x == 0 ? 1 : x, y == 0 ? 1 : y};
        } else {
            result = null;
        }

        return result;
    }


    /**
     * Parse out the line and column position from a sparql parse error message
     *
     * @param str
     * @return an array [line, col] or null if the input could not be matched
     */
    public static int[] parseLineAndCol(String str) {
        Matcher m = posPattern.matcher(str);

        int[] result;
        if(m.find()) {
            int line = Integer.parseInt(m.group(2));
            int col = Integer.parseInt(m.group(4));
            result = new int[] {line, col};
        } else {
            result = null; // new int[] {0, 0};
        }

        return result;
    }

    public static int[] parseLineAndCol(QueryParseException e) {
        int[] tmp = parseRawLineAndCol(e);
        int[] result = adjustLineAndCol(tmp);
        return result;
    }


    public static int[] parseRawLineAndCol(QueryParseException e) {
        String msg = e.getMessage();
        int[] result = parseLineAndCol(msg);
        return result;
    }

    public static int[] lineAndCol(QueryParseException e) {
        int l = e.getLine();
        int c = e.getColumn();
        return new int[] {l == -1 ? Integer.MAX_VALUE : l, c == -1 ? Integer.MAX_VALUE : c };
    }

    /**
     * Creates a new QueryParseException from the provided one with line and column
     * set the the given values.
     * Attaches the original exception as a suppressed one - in order for the newly generated
     * exception to appear as a root cause.
     *
     * The returned exception is presently always of type QueryParseException; subtypes may be considered in the future.
     */
    public static QueryParseException copyAndAdjust(QueryParseException e, int line, int column) {
        String rawMsg = e.getMessage();
        Matcher m = posPattern.matcher(rawMsg);
        StringBuilder sb = new StringBuilder();
        if (m.find()) {
            m.appendReplacement(sb, "$1" + Integer.toString(line) + "$3" + Integer.toString(column));
        }
        m.appendTail(sb);
        String adjustedMsg = sb.toString();

        QueryParseException result = new QueryParseException(adjustedMsg, line, column);
        result.addSuppressed(e);
        return result;
    }

    public static int doCompare(QueryParseException a, QueryParseException b) {
        // A line / column value of -1 seems to indicate successful parsing,
        // but an error in post processing,
        // such as out of scope variables - so (-1, -1) has to be treated differently

        // - this is inconsistent with our assumption that higher
        // line / col numbers indicate greater progress in processing
        // So we adjust -1 to Integer MAX_VALUE

        int[] aa = lineAndCol(a);
        int[] bb = lineAndCol(b);

        int result = ComparisonChain.start()
            .compare(bb[0], aa[0])
            .compare(bb[1], aa[1])
            .result();

        return result;
    }
}
