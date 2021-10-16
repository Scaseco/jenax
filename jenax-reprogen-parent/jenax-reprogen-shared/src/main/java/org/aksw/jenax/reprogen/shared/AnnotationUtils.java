package org.aksw.jenax.reprogen.shared;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.ToString;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationUtils {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationUtils.class);


    public static String deriveIriFromMethod(Method method, PrefixMapping pm) {
        String result = null;

        Iri iri = method.getAnnotation(Iri.class);
        IriNs iriNs = method.getAnnotation(IriNs.class);
        if(iri != null) {
            String rdfPropertyStr = iri.value();

            // Always expand URIs
            // FIXME This will break for general paths - perform prefix expansion using a path transformer!
            String expanded = pm.expandPrefix(rdfPropertyStr);
            // String pathStr = "<" + expanded + ">";

            // result = (P_Path0)PathParser.parse(pathStr, pm);
            result = expanded;

            //logger.debug("Parsed bean property RDF annotation " + pathStr + " into " + result + " on " + method);
            if(logger.isDebugEnabled()) {
                logger.debug("Found @Iri annotation on " + method + ":");
                if(Objects.equals(rdfPropertyStr, expanded)) {
                    logger.debug("  " + rdfPropertyStr);
                } else {
                    logger.debug("  " + rdfPropertyStr + " expanded to " + result);
                }
            }

            //Node p = NodeFactory.createURI(rdfPropertyStr);

            //result = new P_Link(p);
        } else if(iriNs != null) {
            String ns = iriNs.value();
            String uri;
            // If there is a colon we assume a IRI prefix - otherwise we assume a namespace
            // <schema>: part - i.e. whether there is a colon
            if(ns.contains(":")) {
                uri = ns;
            } else {
                uri = pm.getNsPrefixURI(ns);
                if(uri == null) {
                    throw new RuntimeException("Undefined prefix: " + ns + " on method " + method);
                }
            }

            String localName = deriveBeanPropertyName(method.getName());

            result = uri + localName;
            //result = (P_Path0)PathParser.parse(uri + localName, pm);
        }

//		System.out.println(method + " -> " + result);
//		if(result != null && result.toString().contains("style")) {
//			System.out.println("style here");
//		}
        return result;
    }

    public static String deriveBeanPropertyName(String methodName) {
        // TODO Check whether the subsequent character is upper case
        List<String> prefixes = Arrays.asList("get", "set", "is");

        String usedPrefix = prefixes.stream()
                .filter(methodName::startsWith)
                .findAny()
                .orElse(null);

        String result = usedPrefix != null ? methodName.substring(usedPrefix.length()) : methodName;

        // TODO We may want to use the Introspector's public decapitalize method
        result = Introspector.decapitalize(result);
        //result = StringUtils.uncapitalize(result);

        return result;
    }


    public static P_Path0 derivePathFromMethod(Method method, PrefixMapping pm) {
        String iri = AnnotationUtils.deriveIriFromMethod(method, pm);
        P_Path0 result = iri == null ? null : new P_Link(NodeFactory.createURI(iri));
        return result;
    }

    public static Set<String> indexToStringByBeanPropertyName(Class<?> clazz) {
        Set<String> result = new LinkedHashSet<>();
        for(Method method : clazz.getMethods()) {
            String methodName = method.getName();
            String beanPropertyName = deriveBeanPropertyName(methodName);
            ToString toString = method.getAnnotation(ToString.class);

            if(toString != null) {
                result.add(beanPropertyName);
            }
        }

        return result;
    }

    public static Map<String, P_Path0> indexPathsByBeanPropertyName(Class<?> clazz, PrefixMapping pm) {
        Map<String, P_Path0> result = new LinkedHashMap<>();
        for(Method method : clazz.getMethods()) {
            String methodName = method.getName();
            String beanPropertyName = deriveBeanPropertyName(methodName);
            P_Path0 path = derivePathFromMethod(method, pm);

            if(path != null) {
                result.put(beanPropertyName, path);
            }
        }

        return result;
    }

}
