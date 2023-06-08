package org.aksw.jenax.model.prefix.domain.api;

import java.util.Map;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;

public class PrefixSetUtils {
    public static PrefixMap collect(Iterable<PrefixSet> prefixSets) {
        PrefixMap result = PrefixMapFactory.createForOutput();

        for(PrefixSet ps : prefixSets) {
            Map<String, String> mapping = ps.getMap();
            result.putAll(mapping);
        }
        return result;
    }
}
