package org.aksw.jena_sparql_api.sparql.ext.mvn;

import org.aksw.commons.model.maven.domain.api.MavenEntityCore;
import org.aksw.jenax.annotation.reprogen.DefaultValue;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.arq.functionbinder.FunctionBinders;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.norse.term.core.NorseTerms;
import org.apache.jena.graph.Node;

public class JenaExtensionsMvn {
    public static final String ns = "http://jsa.aksw.org/fn/mvn/";


    public static void register() {
        FunctionBinders.getDefaultFunctionBinder()
            .registerAll(JenaExtensionsMvn.class);
    }

    @IriNs(NorseTerms.NS + "mvn.")
    @IriNs(value=ns, deprecated=true)
    public static String toPath(Node mvnIdOrUrn,
            @DefaultValue("snapshots") String snapshotPrefix,
            @DefaultValue("internal") String internalPrefix) {
        String str = NodeUtils.getIriOrString(mvnIdOrUrn);
        MavenEntityCore entity = MavenEntityCore.parse(str);
        String result = MavenEntityCore.toPath(entity, snapshotPrefix, internalPrefix, "/", true, true, true);
        return result;
    }
}
