package org.aksw.jenax.arq.aggregation;

import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;

public class AccDatasetGraph
    implements Acc<DatasetGraph>
{
    private DatasetGraph datasetGraph;
    private QuadPattern quadPattern;

    public AccDatasetGraph(QuadPattern quadPattern) {
       this(DatasetGraphFactory.createGeneral(), quadPattern);
    }

    public AccDatasetGraph(DatasetGraph datasetGraph, QuadPattern quadPattern) {
        super();
        this.datasetGraph = datasetGraph;
        this.quadPattern = quadPattern;
    }

    @Override
    public void accumulate(Binding binding, FunctionEnv env) {
        NodeTransform transform = BindingUtils.asNodeTransform(binding);
        QuadPattern inst = NodeTransformLib.transform(transform, quadPattern);

        for(Quad quad : inst) {
            datasetGraph.add(quad);
        }
    }

    @Override
    public DatasetGraph getValue() {
        return datasetGraph;
    }

}
