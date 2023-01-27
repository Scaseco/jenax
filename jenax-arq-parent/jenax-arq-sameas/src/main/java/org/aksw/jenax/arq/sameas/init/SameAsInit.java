package org.aksw.jenax.arq.sameas.init;

import org.aksw.jenax.arq.sameas.assembler.DatasetAssemblerSameAs;
import org.aksw.jenax.arq.sameas.assembler.SameAsVocab;
import org.aksw.jenax.arq.sameas.dataset.DatasetGraphSameAs;
import org.aksw.jenax.arq.sameas.model.SameAsConfig;
import org.aksw.jenax.arq.uniondefaultgraph.assembler.DatasetAssemblerUnionDefaultGraph;
import org.aksw.jenax.arq.uniondefaultgraph.assembler.UnionDefaultGraphVocab;
import org.aksw.jenax.arq.util.exec.QueryExecUtils;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
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

        ServiceExecutorRegistry.get().addSingleLink(
            (opExec, opOrig, binding, execCxt, chain) -> {
                QueryIterator r;
                if (opExec.getService().getURI().startsWith("sameAs")) {
                    DatasetGraph dsg = DatasetGraphSameAs.wrap(execCxt.getDataset());
                    r = QueryExecUtils.execute(opExec.getSubOp(), dsg, binding, execCxt.getContext());
                } else {
                    r = chain.createExecution(opExec, opOrig, binding, execCxt);
                }
                return r;
            });
    }

    static void registerWith(AssemblerGroup g) {
        AssemblerUtils.register(g, SameAsVocab.DatasetSameAs, new DatasetAssemblerSameAs(), DatasetAssembler.getType());

        AssemblerUtils.register(g, UnionDefaultGraphVocab.DatasetUnionDefaultGraph, new DatasetAssemblerUnionDefaultGraph(false), DatasetAssembler.getType());
        AssemblerUtils.register(g, UnionDefaultGraphVocab.DatasetAutoUnionDefaultGraph, new DatasetAssemblerUnionDefaultGraph(true), DatasetAssembler.getType());
    }
}
