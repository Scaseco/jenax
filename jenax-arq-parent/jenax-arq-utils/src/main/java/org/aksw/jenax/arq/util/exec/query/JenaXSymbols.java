package org.aksw.jenax.arq.util.exec.query;

import org.apache.jena.sparql.util.Symbol;

public class JenaXSymbols {
    /**
     * Symbol for a jenax-based ResourceManager.
     * Certain JenaX components (such as the JDBC extensions for RML) need to
     * dynamically allocate resources during query execution that eventually need to be freed.
     * These components will check the Context for a resource manager and register the clean up actions there.
     */
    public static final Symbol symResourceMgr = Symbol.create("JenaXResourceMgr");
}
