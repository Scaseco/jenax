package org.aksw.jenax.arq.util.security;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.SystemARQ;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

/**
 * Declarations of ARQ context symbols used by JenaX to implement security policies.
 */
public class ArqSecurity {
    /**
     * Context symbol to allow access to the file:// protocol in URLs.
     * File access is disabled by default and must be explicitly enabled.
     */
    public static final Symbol symAllowFileAccess = SystemARQ.allocSymbol("allowFileAccess");

    public static boolean isFileAccessEnabled(Context cxt) {
        Context effectiveCxt = cxt == null ? ARQ.getContext() : cxt;
        boolean result = effectiveCxt.isTrue(symAllowFileAccess);
        return result;
    }

    public static void requireFileAccess(Context cxt) {
        if (!isFileAccessEnabled(cxt)) {
            throw new SecurityException("Access to files is disallowed");
        }
    }
}
