package org.aksw.jenax.model.csvw.plugin;

import org.aksw.jenax.model.csvw.domain.api.Dialect;
import org.aksw.jenax.model.csvw.domain.api.Table;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginCsvw
    implements JenaSubsystemLifecycle {

    public void start() {
        JenaPluginCsvw.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
            Dialect.class, Table.class
        );
    }
}
