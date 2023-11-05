package org.aksw.jenax.model.voidx.plugin;

import org.aksw.jenax.model.voidx.api.VoidClassPartition;
import org.aksw.jenax.model.voidx.api.VoidDataset;
import org.aksw.jenax.model.voidx.api.VoidPropertyPartition;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginVoid
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
            VoidDataset.class,
            VoidClassPartition.class,
            VoidPropertyPartition.class
        );
    }

    public static void init(Personality<RDFNode> p) {
    }
}
