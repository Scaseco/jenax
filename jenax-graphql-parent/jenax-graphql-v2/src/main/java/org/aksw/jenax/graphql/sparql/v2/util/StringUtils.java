package org.aksw.jenax.graphql.sparql.v2.util;

import java.math.BigInteger;
import java.util.function.Predicate;

public class StringUtils {
    /**
     * Return the substring of a string that only consists of digits.
     * <p>
     * Examples:
     * <pre>
     *   "abc123" -&gt; "123"
     *   "abc" -&gt; ""
     *   "abc123.456" -&gt; "456"
     * </pre>
     */
    public static String numberSuffix(String base) {
        int l = base.length();
        int i;
        for (i = l - 1; i >= 0; --i) {
            char c = base.charAt(i);
            if (!Character.isDigit(c)) {
                break;
            }
        }
        String result = base.substring(i + 1, l);
        return result;
    }

    public static String allocateName(String base, boolean forceNumberSuffix, Predicate<String> skip) {
        String result = null;
        if (!forceNumberSuffix) {
            if (!skip.test(base)) {
                result = base;
            }
        }

        if (result == null) {
            String numberStr = numberSuffix(base);
            String prefix = base.substring(0, base.length() - numberStr.length());

            BigInteger current = numberStr.isEmpty()
                    ? BigInteger.valueOf(0)
                    : new BigInteger(numberStr);

            BigInteger one = BigInteger.valueOf(1);

            while (true) {
                current = current.add(one);
                result = prefix + current;
                if (!skip.test(result)) {
                    break;
                }
            }
        }
        return result;
    }
}
