package org.aksw.jenax.arq.engine.quad.plugin;

import org.aksw.jenax.arq.engine.quad.assembler.DatasetAssemblerQuads;
import org.aksw.jenax.arq.engine.quad.assembler.DatasetAssemblerQuadsVocab;
import org.aksw.jenax.arq.engine.quad.assembler.RdfDatasetAssemblerQuads;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class JenaPluginDatasetQuads
    implements JenaSubsystemLifecycle
{
    public static final int LEVEL = 2345;

    @Override
    public void start() {
        init();
    }

    @Override
    public void stop() {
        // Nothing to do
    }

    /** The plugin is loaded late with level {@value #LEVEL} so that the query engine factory
     * is added closer to the front of the registry */
    @Override
    public int level() {
        return LEVEL;
    }

    public static void init() {
        JenaPluginUtils.registerResourceClasses(RdfDatasetAssemblerQuads.class);
        registerWith(Assembler.general);
    }

    static void registerWith(AssemblerGroup g) {
        AssemblerUtils.register(g, DatasetAssemblerQuadsVocab.DatasetQuads, new DatasetAssemblerQuads(), DatasetAssembler.getGeneralType());
    }
}
