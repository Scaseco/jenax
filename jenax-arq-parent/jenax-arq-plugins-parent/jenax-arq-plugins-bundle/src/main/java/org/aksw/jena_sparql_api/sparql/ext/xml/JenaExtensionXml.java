package org.aksw.jena_sparql_api.sparql.ext.xml;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.sparql.pfunction.PropertyFunctionRegistry;

public class JenaExtensionXml {

    public static void register() {
        TypeMapper.getInstance().registerDatatype(RDFDatatypeXml.get());

        FunctionRegistry.get().put(NorseTermsXml.parse, E_XmlParse.class);
        PropertyFunctionRegistry.get().put(NorseTermsXml.parse, new PropertyFunctionFactoryXmlParse());

        FunctionRegistry.get().put(NorseTermsXml.path, E_XPath.class);
        FunctionRegistry.get().put(NorseTermsXml.text, E_XmlToText.class);

        PropertyFunctionRegistry.get().put(NorseTermsXml.unnest, new PropertyFunctionFactoryXmlUnnest());

        // Legacy

        FunctionRegistry.get().put(SparqlExtXmlTerms.parse, E_XmlParse.class);
        PropertyFunctionRegistry.get().put(SparqlExtXmlTerms.parse, new PropertyFunctionFactoryXmlParse());

        FunctionRegistry.get().put(SparqlExtXmlTerms.path, E_XPath.class);
        FunctionRegistry.get().put(SparqlExtXmlTerms.text, E_XmlToText.class);

        PropertyFunctionRegistry.get().put(SparqlExtXmlTerms.unnest, new PropertyFunctionFactoryXmlUnnest());
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("xml", SparqlExtXmlTerms.ns);
    }
}
