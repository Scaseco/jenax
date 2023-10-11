package org.aksw.jenax.sparql.rx.op;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.aksw.commons.lambda.serializable.SerializableFunction;
import org.aksw.commons.lambda.serializable.SerializableSupplier;
import org.aksw.commons.rx.function.RxFunction;
import org.aksw.commons.rx.op.FlowableOperatorCollapseRuns;
import org.aksw.commons.util.stream.CollapseRunsSpec;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;


/**
 * Functions for transforming a Flowable&lt;Quad&gt;
 *
 * @author raven
 *
 */
public class FlowOfQuadsOps {

    public static RxFunction<Quad, DatasetGraph> graphsFromConsecutiveQuads(
            SerializableFunction<Quad, Node> grouper,
            SerializableSupplier<DatasetGraph> graphSupplier) {
        return upstream -> upstream
                .compose(FlowOfQuadsOps.groupConsecutiveQuadsRaw(grouper, graphSupplier))
                .map(Entry::getValue);
    }

    public static RxFunction<Quad, Dataset> datasetsFromConsecutiveQuads(
            SerializableFunction<Quad, Node> grouper,
            SerializableSupplier<DatasetGraph> graphSupplier) {
        return upstream -> upstream
                .compose(FlowOfQuadsOps.groupConsecutiveQuadsRaw(grouper, graphSupplier))
                .map(Entry::getValue)
                .map(DatasetFactory::wrap)
                ;
    }

    public static RxFunction<Quad, Dataset> datasetsFromConsecutiveQuads(
            SerializableSupplier<? extends DatasetGraph> graphSupplier) {
        return upstream -> upstream
                .compose(FlowOfQuadsOps.groupConsecutiveQuadsRaw(Quad::getGraph, graphSupplier))
                .map(Entry::getValue)
                .map(DatasetFactory::wrap)
                ;
    }

    public static RxFunction<Quad, Entry<Node, DatasetGraph>> groupConsecutiveQuadsRaw(
                SerializableFunction<Quad, Node> grouper,
                SerializableSupplier<? extends DatasetGraph> graphSupplier) {

        return FlowableOperatorCollapseRuns.<Quad, Node, DatasetGraph>create(CollapseRunsSpec.create(
                grouper::apply,
                groupKey -> graphSupplier.get(),
                DatasetGraph::add)).transformer()::apply;
    }

    public static RxFunction<Quad, Entry<Node, Graph>> groupConsecutiveQuadsToGraph(
            SerializableFunction<Quad, Node> grouper,
            SerializableFunction<Quad, Triple> toTriple,
            SerializableSupplier<? extends Graph> graphSupplier) {

        return FlowableOperatorCollapseRuns.<Quad, Node, Graph>create(CollapseRunsSpec.create(
                grouper::apply,
                groupKey -> graphSupplier.get(),
                (graph, quad) -> graph.add(toTriple.apply(quad)))).transformer()::apply;
    }

    public static RxFunction<Quad, Entry<Node, List<Quad>>> groupToList()
    {
        return FlowableOperatorCollapseRuns.<Quad, Node, List<Quad>>create(CollapseRunsSpec.create(
                Quad::getGraph,
                graph -> new ArrayList<>(),
                (list, item) -> list.add(item)
           )).transformer()::apply;
    }

}
