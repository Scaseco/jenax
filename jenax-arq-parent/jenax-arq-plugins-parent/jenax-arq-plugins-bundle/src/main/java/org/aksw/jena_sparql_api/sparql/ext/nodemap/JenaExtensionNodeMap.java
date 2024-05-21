package org.aksw.jena_sparql_api.sparql.ext.nodemap;

import org.aksw.jenax.norse.term.rdf.NorseTermsNodeMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionNodeMap {
    public static void register() {
        // Datatypes based on only on Jena are registered in the
        // jenax-arq-plugins-datatypes module
        // TypeMapper typeMapper = TypeMapper.getInstance();
        // typeMapper.registerDatatype(RDFDatatypeBinding.get());

        FunctionRegistry fnRegistry = FunctionRegistry.get();
        fnRegistry.put(NorseTermsNodeMap.strictGet, F_NodeMapStrictGet.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        // pm.setNsPrefix("nodeMap", NorseBindingTerms.NS + "nodeMap");
    }
}
