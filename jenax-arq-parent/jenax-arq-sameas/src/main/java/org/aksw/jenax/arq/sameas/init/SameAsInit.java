package org.aksw.jenax.arq.sameas.init;

import org.aksw.jenax.arq.sameas.assembler.DatasetAssemblerSameAs;
import org.aksw.jenax.arq.sameas.assembler.SameAsVocab;
import org.aksw.jenax.arq.sameas.model.SameAsConfig;
import org.aksw.jenax.arq.uniondefaultgraph.assembler.DatasetAssemblerUnionDefaultGraph;
import org.aksw.jenax.arq.uniondefaultgraph.assembler.UnionDefaultGraphVocab;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sys.JenaSubsystemLifecycle;

public class SameAsInit
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
        JenaPluginUtils.registerResourceClasses(SameAsConfig.class);
        registerWith(Assembler.general);
    }

    static void registerWith(AssemblerGroup g) {
        AssemblerUtils.register(g, SameAsVocab.DatasetSameAs, new DatasetAssemblerSameAs(), DatasetAssembler.getType());

        AssemblerUtils.register(g, UnionDefaultGraphVocab.DatasetUnionDefaultGraph, new DatasetAssemblerUnionDefaultGraph(false), DatasetAssembler.getType());
        AssemblerUtils.register(g, UnionDefaultGraphVocab.DatasetAutoUnionDefaultGraph, new DatasetAssemblerUnionDefaultGraph(true), DatasetAssembler.getType());

    }
}
