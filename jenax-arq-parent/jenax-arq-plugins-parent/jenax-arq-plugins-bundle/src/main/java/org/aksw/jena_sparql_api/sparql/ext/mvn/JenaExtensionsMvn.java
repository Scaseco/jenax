package org.aksw.jena_sparql_api.sparql.ext.mvn;

import org.aksw.commons.model.maven.domain.api.MavenEntityCore;
import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.aksw.jenax.norse.term.core.NorseTerms;

public class JenaExtensionsMvn {
    public static final String ns = "http://jsa.aksw.org/fn/mvn/";


    public static void register() {
        FunctionBinders.getDefaultFunctionBinder()
            .registerAll(JenaExtensionsMvn.class);
    }

    @IriNs(NorseTerms.NS + "mvn.")
    @IriNs(value=ns, deprecated=true)
    public static String toPath(String mvnIdOrUrn,
            @DefaultValue("snapshots") String snapshotPrefix,
            @DefaultValue("internal") String internalPrefix) {

        MavenEntityCore entity = MavenEntityCore.parse(mvnIdOrUrn);
        String result = MavenEntityCore.toPath(entity, snapshotPrefix, internalPrefix, "/", true, true, true);
        return result;
    }
}
