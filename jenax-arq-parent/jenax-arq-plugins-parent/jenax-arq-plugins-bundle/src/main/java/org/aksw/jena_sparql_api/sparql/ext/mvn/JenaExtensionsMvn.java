package org.aksw.jena_sparql_api.sparql.ext.mvn;

import org.aksw.dcat.jena.domain.api.MavenEntityCore;
import org.aksw.jena_sparql_api.sparql.ext.util.JenaExtensionUtil;
import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.aksw.jenax.annotation.reprogen.IriNs;

public class JenaExtensionsMvn {
    public static final String ns = "http://jsa.aksw.org/fn/mvn/";


    public static void register() {
        JenaExtensionUtil.getDefaultFunctionBinder()
            .registerAll(JenaExtensionsMvn.class);
    }

    @IriNs(ns)
    public static String toPath(String mvnIdOrUrn,
            @DefaultValue("snapshots") String snapshotPrefix,
            @DefaultValue("internal") String internalPrefix) {

        MavenEntityCore entity = MavenEntityCore.parse(mvnIdOrUrn);
        String result = MavenEntityCore.toPath(entity, snapshotPrefix, internalPrefix, "/", true, true, true);
        return result;
    }
}
