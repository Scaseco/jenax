package org.aksw.jenax.io.rdf.jsonld;

import org.aksw.jenax.io.rdf.json.JsonProvider;
import org.apache.jena.graph.Node;


public class JsonLdAccessorImpl {

    // Json provider could be reused from JSONPath
    // protected JsonProvider jsonProvider;
    // protected RdfElementFactory eltProvider;
    protected JsonProvider jsonProvider;

    public Object getOrCreateContext(Object json) {
        Object result = jsonProvider.getOrCreateObject(json, JsonLdTerms.context);
        return result;
    }

    /** Declare a property on a json object (not its context) */
    public void declareProperty(Object json, String fieldName, Node property, boolean isForward) {
        Object context = getOrCreateContext(json);
        Object fieldMetaData = jsonProvider.computePropertyIfAbsent(context, fieldName, k -> jsonProvider.newObject());
        if (!jsonProvider.isObject(fieldMetaData)) {
            throw new RuntimeException("Context is not an object");
        }

        // XXX Add support for generalized RDF with non-IRI properties
        String iriStr = property.getURI();
        Object iriValue = jsonProvider.newLiteral(iriStr);
        if (isForward) {
            // Clear an existing reverse value
            jsonProvider.removeProperty(fieldMetaData, JsonLdTerms.reverse);
            jsonProvider.setProperty(fieldMetaData, JsonLdTerms.id, iriValue);
        } else {
            // Clear existing @id value
            jsonProvider.removeProperty(fieldMetaData, JsonLdTerms.id);
            jsonProvider.setProperty(fieldMetaData, JsonLdTerms.reverse, iriValue);
        }
    }

    public void declareNamespace(Object json, String prefix, String iri) {
        Object context = getOrCreateContext(json);
        jsonProvider.setProperty(context, prefix, iri);
    }

    public void getNamespaces(Object json) {
        // Object context = jsonProvider.getC
    }

    // Get namespaces - all context entries that are not also fields
}
