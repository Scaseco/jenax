package org.aksw.jenax.arq.sameas.init;

import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.jenax.arq.sameas.assembler.DatasetAssemblerRdfsReduced;
import org.aksw.jenax.arq.sameas.assembler.DatasetAssemblerRdfsReducedEnable;
import org.aksw.jenax.arq.sameas.assembler.DatasetAssemblerSameAs;
import org.aksw.jenax.arq.sameas.assembler.SameAsVocab;
import org.aksw.jenax.arq.sameas.model.SameAsConfig;
import org.aksw.jenax.arq.uniondefaultgraph.assembler.DatasetAssemblerUnionDefaultGraph;
import org.aksw.jenax.arq.uniondefaultgraph.assembler.UnionDefaultGraphVocab;
import org.aksw.jenax.arq.util.dataset.DatasetGraphRDFSReduced;
import org.aksw.jenax.arq.util.dataset.DatasetGraphSameAs;
import org.aksw.jenax.arq.util.dataset.DatasetGraphUnionDefaultGraph;
import org.aksw.jenax.arq.util.exec.query.QueryExecUtils;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfs.SetupRDFS;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.engine.ExecutionContext;
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

        registerServiceWrapper("sameAs", execCxt ->
            DatasetGraphSameAs.wrap(DatasetGraphUnionDefaultGraph.wrapIfNeeded(execCxt.getDataset())));

        registerServiceWrapper("sameAs+rdfs", execCxt -> {
            SetupRDFS setup = Objects.requireNonNull(execCxt.getContext().get(DatasetAssemblerRdfsReduced.symSetupRdfsNode), "No RDFS setup found in the active dataset context");
            return DatasetGraphRDFSReduced.wrap(DatasetGraphSameAs.wrap(DatasetGraphUnionDefaultGraph.wrapIfNeeded(execCxt.getDataset())), setup);
        });

        registerServiceWrapper("rdfs", execCxt -> {
            SetupRDFS setup = Objects.requireNonNull(execCxt.getContext().get(DatasetAssemblerRdfsReduced.symSetupRdfsNode), "No RDFS setup found in the active dataset context");
            return DatasetGraphRDFSReduced.wrap(DatasetGraphUnionDefaultGraph.wrapIfNeeded(execCxt.getDataset()), setup);
        });
    }

    public static void registerServiceWrapper(String serviceName, Function<ExecutionContext, ? extends DatasetGraph> wrapper) {
        Pattern servicePattern = Pattern.compile("^" + Pattern.quote(serviceName) + "($|:)");
        ServiceExecutorRegistry.get().addSingleLink(
            (opExec, opOrig, binding, execCxt, chain) -> {
                QueryIterator r = null;
                Node node = opExec.getService();
                if (node != null && node.isURI()) {
                    String uri = node.getURI();
                    Matcher m = servicePattern.matcher(uri);
                    if (m.find()) {
                        if (m.end() < uri.length()) {
                            throw new RuntimeException("Trailing characters found after '" + m.group() + "': " + uri.substring(m.end()));
                        }
                        // Inherit union default graph if backed by tdb
                        DatasetGraph adhocDs = wrapper.apply(execCxt);
                        r = QueryExecUtils.execute(opExec.getSubOp(), adhocDs, binding, execCxt.getContext());
                    }
                }
                if (r == null) {
                    r = chain.createExecution(opExec, opOrig, binding, execCxt);
                }
                return r;
            });
    }

    static void registerWith(AssemblerGroup g) {
        AssemblerUtils.register(g, SameAsVocab.DatasetSameAs, new DatasetAssemblerSameAs(), DatasetAssembler.getGeneralType());

        AssemblerUtils.register(g, DatasetAssemblerRdfsReduced.getType(), new DatasetAssemblerRdfsReduced(), DatasetAssembler.getGeneralType());
        AssemblerUtils.register(g, DatasetAssemblerRdfsReducedEnable.getType(), new DatasetAssemblerRdfsReducedEnable(), DatasetAssembler.getGeneralType());

        AssemblerUtils.register(g, UnionDefaultGraphVocab.DatasetUnionDefaultGraph, new DatasetAssemblerUnionDefaultGraph(false), DatasetAssembler.getGeneralType());
        AssemblerUtils.register(g, UnionDefaultGraphVocab.DatasetAutoUnionDefaultGraph, new DatasetAssemblerUnionDefaultGraph(true), DatasetAssembler.getGeneralType());
    }
}
