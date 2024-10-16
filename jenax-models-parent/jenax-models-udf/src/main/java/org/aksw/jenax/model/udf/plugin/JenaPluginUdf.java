package org.aksw.jenax.model.udf.plugin;


import org.aksw.jenax.model.udf.api.InverseDefinition;
import org.aksw.jenax.model.udf.api.UdfDefinition;
import org.aksw.jenax.model.udf.api.UserDefinedFunctionResource;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginUdf
    implements JenaSubsystemLifecycle
{
    public void start() {
        init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        JenaPluginUtils.registerResourceClasses(
                UserDefinedFunctionResource.class,
                InverseDefinition.class,
                // PrefixDefinition.class,
                // PrefixSet.class,
                UdfDefinition.class
                );
    }

    public static void init(Personality<RDFNode> p) {
    }
}
