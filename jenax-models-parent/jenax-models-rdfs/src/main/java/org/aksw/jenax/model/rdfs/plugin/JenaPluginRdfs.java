package org.aksw.jenax.model.rdfs.plugin;

import org.aksw.jenax.model.rdfs.domain.api.HasRdfsComment;
import org.aksw.jenax.model.rdfs.domain.api.HasRdfsLabel;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginRdfs
    implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        JenaPluginRdfs.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
                HasRdfsLabel.class,
                HasRdfsComment.class);
    }
}
