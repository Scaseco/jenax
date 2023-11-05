package org.aksw.jenax.model.table.plugin;

import org.aksw.jenax.model.table.domain.api.ColumnItem;
import org.aksw.jenax.model.table.domain.api.TableDef;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;


public class JenaPluginTable
    implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        JenaPluginTable.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
                TableDef.class,
                ColumnItem.class);
    }
}
