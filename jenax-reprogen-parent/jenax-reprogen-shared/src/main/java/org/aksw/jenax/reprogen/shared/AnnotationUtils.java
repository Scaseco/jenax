package org.aksw.jenax.reprogen.shared;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.ToString;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationUtils {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationUtils.class);


    /** Legacy method - should no longer be used */
//    public static String deriveIriFromMethod(Method method, PrefixMapping pm) {
//
//    }

    public static List<String> deriveIrisFromMethod(Method method, PrefixMapping pm) {
        List<String> result = new ArrayList<>();
        Iri[] iris = method.getAnnotationsByType(Iri.class);
        // String[] rdfPropertyStrs = iris.value();
        // for (String rdfPropertyStr : rdfPropertyStrs) {
        for (Iri iri : iris) {
            String rdfPropertyStr = iri.value();
            // Always expand URIs
            // FIXME This will break for general paths - perform prefix expansion using a path transformer!
            String expanded = pm.expandPrefix(rdfPropertyStr);
            // String pathStr = "<" + expanded + ">";

            // result = (P_Path0)PathParser.parse(pathStr, pm);
            String contrib = expanded;

            //logger.debug("Parsed bean property RDF annotation " + pathStr + " into " + result + " on " + method);
            if(logger.isDebugEnabled()) {
                logger.debug("Found @Iri annotation on " + method + ":");
                if(Objects.equals(rdfPropertyStr, expanded)) {
                    logger.debug("  " + rdfPropertyStr);
                } else {
                    logger.debug("  " + rdfPropertyStr + " expanded to " + result);
                }
            }

            result.add(contrib);
        }
        //Node p = NodeFactory.createURI(rdfPropertyStr);

        //result = new P_Link(p);

        IriNs[] iriNss = method.getAnnotationsByType(IriNs.class);
        String localName = deriveBeanPropertyName(method.getName());
        // String[] nss = iriNs.value();
        // for (String ns : nss) {
        for (IriNs iriNs : iriNss) {
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

            String contrib = uri + localName;
            result.add(contrib);
        }
                //result = (P_Path0)PathParser.parse(uri + localName, pm);

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

        String result;
        if (usedPrefix != null) {
            result = methodName.substring(usedPrefix.length());

            // If the method is simply named after the prefix then use the prefix as given
            if (result.isEmpty()) {
                result = usedPrefix;
            }
        } else {
            result = methodName;
        }

        // TODO We may want to use the Introspector's public decapitalize method
        result = Introspector.decapitalize(result);
        //result = StringUtils.uncapitalize(result);

        return result;
    }


    public static List<P_Path0> derivePathsFromMethod(Method method, PrefixMapping pm) {
        List<P_Path0> result = AnnotationUtils.deriveIrisFromMethod(method, pm).stream()
                .map(NodeFactory::createURI)
                .map(P_Link::new)
                .collect(Collectors.toList());
                ;
//        String iri = AnnotationUtils.deriveIriFromMethod(method, pm);
//        P_Path0 result = iri == null ? null : new P_Link(NodeFactory.createURI(iri));
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
            List<P_Path0> paths = derivePathsFromMethod(method, pm);

            for (P_Path0 path : paths) {
                result.put(beanPropertyName, path);
            }
        }

        return result;
    }
}
