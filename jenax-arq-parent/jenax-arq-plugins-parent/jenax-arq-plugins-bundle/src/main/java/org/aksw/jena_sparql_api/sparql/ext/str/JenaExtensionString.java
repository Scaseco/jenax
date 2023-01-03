package org.aksw.jena_sparql_api.sparql.ext.str;

import org.aksw.commons.util.string.StringUtils;
import org.aksw.jena_sparql_api.sparql.ext.util.JenaExtensionUtil;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.functionbinder.FunctionBinder;
import org.apache.jena.shared.PrefixMapping;

public class JenaExtensionString {
    public static final String NS = "http://jsa.aksw.org/fn/str/";

    public static void register() {
        FunctionBinder binder = JenaExtensionUtil.getDefaultFunctionBinder();
        binder.registerAll(JenaExtensionString.class);
    }

    public static void addPrefixes(PrefixMapping pm) {
        pm.setNsPrefix("str", NS);
    }

    /** Convert a string using hyphens to upper camel case */
    @IriNs(NS)
    public static String upperCamel(String str) {
        return StringUtils.toUpperCamelCase(str);
    }

    /** Convert a string using hyphens to lower camel case */
    @IriNs(NS)
    public static String lowerCamel(String str) {
        return StringUtils.toLowerCamelCase(str);
    }
}
