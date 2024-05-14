package org.aksw.jenax.model.geosparql.plugin;

import org.aksw.jenax.model.geosparql.Geometry;
import org.aksw.jenax.model.geosparql.HasGeometry;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginGeoSparql
    implements JenaSubsystemLifecycle {

    @Override
    public void start() {
        JenaPluginGeoSparql.init();
    }

    @Override
    public void stop() {
    }

    public static void init() {
        init(BuiltinPersonalities.model);
    }

    public static void init(Personality<RDFNode> p) {
        JenaPluginUtils.registerResourceClasses(
                HasGeometry.class,
                Geometry.class);
    }
}
