package org.aksw.jenax.model.shacl.util;

import java.util.Map;

import org.aksw.jenax.model.shacl.domain.ShHasPrefixes;
import org.aksw.jenax.model.shacl.domain.ShPrefixMapping;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

public class ShPrefixUtils {
    /** Collect prefixes from the given argument. If it is null then return an empty set of prefixes. */
    public static PrefixMap collect(ShHasPrefixes hasPrefixes) {
        PrefixMap result = PrefixMapFactory.createForOutput();
        if (hasPrefixes != null) {
            collect(result, hasPrefixes.getPrefixes());
        }
        return result;
    }

    public static PrefixMap collect(PrefixMap result, Iterable<ShPrefixMapping> prefixMappings) {
        for (ShPrefixMapping prefixMapping : prefixMappings) {
            Map<String, String> mapping = prefixMapping.getMap();
            result.putAll(mapping);
        }
        return result;
    }
}
