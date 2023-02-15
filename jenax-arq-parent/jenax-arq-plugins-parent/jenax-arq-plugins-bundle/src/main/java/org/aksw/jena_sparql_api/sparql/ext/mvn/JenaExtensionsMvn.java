package org.aksw.jena_sparql_api.sparql.ext.mvn;

import org.aksw.dcat.jena.domain.api.MavenEntityCore;
import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;

public class JenaExtensionsMvn {
    public static final String ns = "http://jsa.aksw.org/fn/mvn/";


    public static void register() {
        FunctionBinders.getDefaultFunctionBinder()
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
