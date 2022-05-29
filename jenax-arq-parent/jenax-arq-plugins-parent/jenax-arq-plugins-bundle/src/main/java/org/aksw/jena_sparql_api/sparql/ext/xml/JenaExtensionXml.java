package org.aksw.jena_sparql_api.sparql.ext.xml;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionXml {
    public static String ns = "http://jsa.aksw.org/fn/xml/";

    public static void register() {
        TypeMapper.getInstance().registerDatatype(RDFDatatypeXml.INSTANCE);

        FunctionRegistry.get().put(ns + "parse", E_XmlParse.class);
        PropertyFunctionRegistry.get().put(ns + "parse", new PropertyFunctionFactoryXmlParse());


        FunctionRegistry.get().put(ns + "path", E_XPath.class);

		PropertyFunctionRegistry.get().put(ns + "unnest", new PropertyFunctionFactoryXmlUnnest());
    }

    public static void addPrefixes(PrefixMapping pm) {
		pm.setNsPrefix("xml", ns);
    }
}
