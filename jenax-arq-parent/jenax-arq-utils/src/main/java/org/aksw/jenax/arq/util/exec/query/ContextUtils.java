package org.aksw.jenax.arq.util.exec.query;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.MappingRegistry;
import org.apache.jena.sparql.util.Symbol;

public class ContextUtils {
    /** Extend a given context with all values from a map */
    public static Context putAll(Context cxt, Map<String, ?> map) {
        for (Entry<String, ?> e : map.entrySet()) {
            String symbolName = MappingRegistry.mapPrefixName(e.getKey());
            Symbol symbol = Symbol.create(symbolName);
            cxt.set(symbol, e.getValue());
        }
        return cxt;
    }
}
