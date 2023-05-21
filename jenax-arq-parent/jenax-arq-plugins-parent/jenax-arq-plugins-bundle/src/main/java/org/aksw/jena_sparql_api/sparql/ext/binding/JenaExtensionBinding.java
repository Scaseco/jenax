package org.aksw.jena_sparql_api.sparql.ext.binding;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;

public class JenaExtensionBinding {
    public static void register() {
        // Datatypes based on only on Jena are registered in the
        // jenax-arq-plugins-datatypes module
        // TypeMapper typeMapper = TypeMapper.getInstance();
        // typeMapper.registerDatatype(RDFDatatypeBinding.get());

        FunctionRegistry fnRegistry = FunctionRegistry.get();
        fnRegistry.put(NorseBindingTerms.get, F_BindingGet.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("binding", NorseBindingTerms.NS);
    }
}
