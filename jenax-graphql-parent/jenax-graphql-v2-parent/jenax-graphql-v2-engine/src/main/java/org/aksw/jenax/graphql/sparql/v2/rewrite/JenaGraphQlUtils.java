package org.aksw.jenax.graphql.sparql.v2.rewrite;

import java.util.List;

import org.aksw.jenax.graphql.sparql.v2.util.GraphQlUtils;
import org.apache.jena.riot.system.PrefixMap;

import graphql.language.Directive;
import graphql.language.DirectivesContainer;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;

public class JenaGraphQlUtils {

    public static PrefixMap collectPrefixes(Directive node, PrefixMap prefixMap) {
        String prefix = GraphQlUtils.getArgAsString(node, "name");
        String iri = GraphQlUtils.getArgAsString(node, "iri");
        if (prefix != null && iri != null) { // XXX Validate
            prefixMap.add(prefix, iri);
        }

        Value<?> map = GraphQlUtils.getArgValue(node, "map");
        if (map != null) {
            JenaGraphQlUtils.readPrefixMap(prefixMap, map);
        }
        return prefixMap;
    }

    public static PrefixMap collectPrefixes(DirectivesContainer<?> directivesContainer, PrefixMap prefixMap) {
        List<Directive> matches = directivesContainer.getDirectives("prefix");
        matches.forEach(directive -> JenaGraphQlUtils.collectPrefixes(directive, prefixMap));
        return prefixMap;
    }

    public static ObjectValue toObjectValue(PrefixMap prefixes) {
        List<ObjectField> fields = prefixes.getMapping().entrySet().stream()
            .map(e -> ObjectField.newObjectField().name(e.getKey()).value(StringValue.of(e.getValue())).build())
            .toList();
        ObjectValue result = ObjectValue.newObjectValue().objectFields(fields).build();
        return result;
    }

    public static PrefixMap readPrefixMap(PrefixMap result, Value<?> value) {
        if (value instanceof ObjectValue) {
            ObjectValue obj = (ObjectValue) value;
            for (ObjectField field : obj.getObjectFields()) {
                String prefix = field.getName();
                String namespace = GraphQlUtils.toString(field.getValue());
                result.add(prefix, namespace);
            }
        }
        // System.out.println(result);
        return result;
    }


    /** Returns true if the directive contains prefixes. Adds them to the given prefixes map. */
    public static boolean readPrefixDirective(Directive node, PrefixMap outPrefixes) {
        boolean result = false;
        String name = node.getName();
        switch (name) {
        case "prefix": {
            String prefix = GraphQlUtils.getArgAsString(node, "name");
            String iri = GraphQlUtils.getArgAsString(node, "iri");
            if (prefix != null && iri != null) { // XXX Validate
                outPrefixes.add(prefix, iri);
            }

            Value<?> map = GraphQlUtils.getArgValue(node, "map");
            if (map != null) {
                JenaGraphQlUtils.readPrefixMap(outPrefixes, map);
            }
            result = true;
            break;
        }
        default:
            break;
        }
        return result;
    }

    public static String resolveIri(String iriStr, PrefixMap prefixMap) {
        String result = null;
        if (iriStr != null && prefixMap != null) {
            result = prefixMap.get(iriStr);
            if (result == null) {
                result = prefixMap.expand(iriStr);
            }
        }

        if (result == null) {
            result = iriStr;
        }

        return result;
    }

    public static String getArgAsIriString(Directive directive, String argName, PrefixMap prefixMap) {
        String iriStr = GraphQlUtils.getArgAsString(directive, argName);
        String result = JenaGraphQlUtils.resolveIri(iriStr, prefixMap);
        return result;
    }

    public static String parseRdfIri(String nodeName, DirectivesContainer<?> directives, String directiveName, String iriArgName, String nsArgName, PrefixMap prefixMap) {
        Directive directive = GraphQlUtils.expectAtMostOneDirective(directives, directiveName);
        String result = directive == null ? null : parseRdfIri(nodeName, directive, iriArgName, nsArgName, prefixMap);
        return result;
    }

    public static String parseRdfIri(String nodeName, Directive directive, String iriArgName, String nsArgName, PrefixMap prefixMap) {
        String finalIri = JenaGraphQlUtils.getArgAsIriString(directive, iriArgName, prefixMap);
        String finalNs = nsArgName == null ? null : JenaGraphQlUtils.getArgAsIriString(directive, nsArgName, prefixMap);

        if (finalIri != null && finalNs != null) {
            System.err.println("Warn: iri and ns are mutually exclusive");
        }

        String finalFieldName = nodeName;
        String result = finalIri != null
                ? finalIri
                : finalNs + finalFieldName;

        return result;
    }
}
