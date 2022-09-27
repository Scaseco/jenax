package org.aksw.jenax.arq.fromasfilter.init;

import org.aksw.jenax.arq.fromasfilter.assembler.DatasetAssemblerFromAsFilter;
import org.aksw.jenax.arq.fromasfilter.assembler.FromAsFilterVocab;
import org.aksw.jenax.arq.fromasfilter.engine.QueryEngineFactoryFromAsFilter;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class FromAsFilterInit
    implements JenaSubsystemLifecycle
{
    @Override
    public void start() {
        init();
    }

    @Override
    public void stop() {
        // Nothing to do
    }

    public static void init() {
        QueryEngineRegistry.addFactory(new QueryEngineFactoryFromAsFilter());

        registerWith(Assembler.general);
    }

    static void registerWith(AssemblerGroup g) {
        AssemblerUtils.register(g, FromAsFilterVocab.DatasetFromAsFilter, new DatasetAssemblerFromAsFilter(), DatasetAssembler.getType());
    }
}
