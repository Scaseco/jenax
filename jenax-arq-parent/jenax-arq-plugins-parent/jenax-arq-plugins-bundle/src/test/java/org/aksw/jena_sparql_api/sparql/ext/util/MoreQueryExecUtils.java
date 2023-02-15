package org.aksw.jena_sparql_api.sparql.ext.util;

import org.aksw.jenax.arq.util.exec.QueryExecSimple;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.sse.SSE;

public class MoreQueryExecUtils {
    public static PrefixMapping createTestPrefixMapping() {
        PrefixMapping result = new PrefixMappingImpl();
        result.setNsPrefixes(SSE.getPrefixMapRead()); // afn, and such (not the right apf though!)
        result.setNsPrefixes(GeoSPARQL_URI.getPrefixes());
        JenaExtensionUtil.addPrefixes(result);

        return result;
    }

    public static final QueryExecSimple INSTANCE = QueryExecSimple.create(createTestPrefixMapping());
}
