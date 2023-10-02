package org.aksw.jenax.sparql.rx.op;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.rx.op.FlowableOperatorSequentialGroupBy;
import org.aksw.commons.util.stream.SequentialGroupBySpec;
import org.aksw.jenax.arq.dataset.api.ResourceInDataset;
import org.aksw.jenax.arq.dataset.impl.ResourceInDatasetImpl;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.sparql.relation.dataset.GraphNameAndNode;
import org.aksw.jenax.sparql.relation.dataset.NodesInDataset;
import org.aksw.jenax.sparql.relation.dataset.NodesInDatasetImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;

import com.google.common.base.Strings;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableTransformer;
import io.reactivex.rxjava3.core.Maybe;

public class FlowOfRdfNodesInDatasetsOps {


    /**
     * Return a flow over the natural resources in the named graphs of a dataset.
     * A natural resource is a node that has has the same name (IRI) as the graph.
     *
     * @param dataset
     * @return
     */
    public static Flowable<ResourceInDataset> naturalResources(Dataset dataset) {
        return Flowable.fromIterable(() -> dataset.listNames())
            .map(graphName -> {
                Node node = NodeFactory.createURI(graphName);
                return new ResourceInDatasetImpl(dataset, graphName, node);
            });
    }

    /**
     * Operator that marks (graph, node) pairs from a dataset using a
     * SELECT query with two result variables and returns them in a
     * NodesInDataset object.
     *
     *
     * @param nodeSelector
     * @return
     */
    public static Function<Dataset, NodesInDataset> mapToNodesInDataset(Query nodeSelector) {

        // TODO Ensure that the query result has two columns
        Function<? super SparqlQueryConnection, Collection<List<Node>>> mapper = ResultSetMappers.createTupleMapper(nodeSelector);

        return dataset -> {
            try(SparqlQueryConnection conn = RDFConnectionFactory.connect(dataset)) {
                Collection<List<Node>> tuples = mapper.apply(conn);

                Set<GraphNameAndNode> gan = tuples.stream()
                    .map(tuple -> {
                        Node g = tuple.get(0);
                        Node node = tuple.get(1);

                        String graphName = g.getURI();

                        return new GraphNameAndNode(graphName, node);
                    })
                    .collect(Collectors.toSet());

                NodesInDataset r = new NodesInDatasetImpl(dataset, gan);
                return r;
            }
        };
    }


    /**
     * Accumulate consecutive ResourceInDataset items which share the same
     * Dataset or underlying DatasetGraph by reference equality into an
     * Entry<Dataset, List<Node>>

     * @return
     */
    public static FlowableTransformer<ResourceInDataset, NodesInDataset> groupedResourceInDataset() {
        return upstream -> upstream
                .lift(FlowableOperatorSequentialGroupBy.create(SequentialGroupBySpec.<ResourceInDataset, Dataset, List<ResourceInDataset>>create(
                        ResourceInDataset::getDataset,
                        (k1, k2) -> k1 == k2 || k1.asDatasetGraph() == k2.asDatasetGraph(),
                        key -> new ArrayList<>(),
                        (list, item) -> { list.add(item); return list; })))
                .map(Entry::getValue)
//                .compose(Transformers.<ResourceInDataset>toListWhile(
//                        (list, t) -> {
//                            boolean r = list.isEmpty();
//                            if(!r) {
//                                ResourceInDataset proto = list.get(0);
//                                r = proto.getDataset() == t.getDataset() ||
//                                    proto.getDataset().asDatasetGraph() == t.getDataset().asDatasetGraph();
//                            }
//                            return r;
//                        }))
                .map(list -> {
                    ResourceInDataset proto = list.get(0);
                    Dataset ds = proto.getDataset();
                    Set<GraphNameAndNode> nodes = list.stream()
                            .map(r -> new GraphNameAndNode(r.getGraphName(), r.asNode()))
                            .collect(Collectors.toSet());

                    return new NodesInDatasetImpl(ds, nodes);
                });
    }

    public static List<ResourceInDataset> ungroupResourceInDataset(NodesInDataset grid) {
        List<ResourceInDataset> result = grid.getGraphNameAndNodes().stream()
                .map(gan -> new ResourceInDatasetImpl(grid.getDataset(), gan.getGraphName(), gan.getNode()))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Intended to be used with flatMap
     *
     * flow.flatMap(...)
     *
     * @param grid
     * @return
     */
    public static Flowable<ResourceInDataset> ungrouperResourceInDataset(NodesInDataset grid) {
        return Flowable.fromIterable(ungroupResourceInDataset(grid));
    }


    public static Function<? super SparqlQueryConnection, Node> createKeyMapper(
            String keyArg,
            Function<? super String, ? extends Query> queryParser,
            Query fallback) {
        //Function<Dataset, Node> keyMapper;

        Query effectiveKeyQuery;
        boolean useFallback = Strings.isNullOrEmpty(keyArg);
        if(!useFallback) {
            effectiveKeyQuery = queryParser.apply(keyArg);
            QueryUtils.optimizePrefixes(effectiveKeyQuery);
        } else {
            effectiveKeyQuery = fallback;
        }

        Function<? super SparqlQueryConnection, Node> result = ResultSetMappers.createNodeMapper(effectiveKeyQuery, NodeFactory.createLiteral(""));
        return result;
    }

    public static Flowable<NodesInDataset> mergeConsecutiveResourceInDatasets(Flowable<NodesInDataset> in) {
        // FIXME This will break if we reuse the flow
        // The merger has to be created on subscription
        ConsecutiveGraphMergerMergerForResourceInDataset merger = new ConsecutiveGraphMergerMergerForResourceInDataset();

        return
            in.flatMapMaybe(x -> Maybe.fromCallable(() -> merger.accept(x).orElse(null)))
            .concatWith(Maybe.fromCallable(() -> merger.getPendingDataset().orElse(null)));
    }


//	public static FlowableTransformer<ResourceInDataset, ResourceInDataset> createSystemSorter2(
//			Function<? super SparqlQueryConnection, Node> keyMapper,
//			List<String> sysCallArgs,
//			boolean mergeConsecutiveDatasets) {
//
////		List<String> sortArgs = SysCalls.createDefaultSortSysCall(cmdSort);
//
//
//		return upstream ->
//			upstream
//				.compose(ResourceInDatasetFlowOps.groupedResourceInDataset())
//				.map(group -> {
//					Dataset ds = group.getDataset();
//					Node key;
//					try(RDFConnection conn = RDFConnectionFactory.connect(ds)) {
//						key = keyMapper.apply(conn);
//					}
//					return Maps.immutableEntry(key, group);
//				})
//				.map(e -> DatasetFlowOps.serializeForSort(DatasetFlowOps.gson, e.getKey(), e.getValue()))
//				.compose(FlowableOps.sysCall(sysCallArgs))
//				.map(line -> DatasetFlowOps.deserializeFromSort(DatasetFlowOps.gson, line, NodesInDataset.class))
//				.flatMap(ResourceInDatasetFlowOps::ungrouperResourceInDataset)
//			;
//	}


    /**
     * Adapter to create a transformed for {@link ResourceInDataset} based on one for {@link NodesInDataset}.
     *
     * @param innerTransform
     * @return
     */
    public static FlowableTransformer<ResourceInDataset, ResourceInDataset> createTransformerFromGroupedTransform(FlowableTransformer<NodesInDataset, NodesInDataset> innerTransform) {
        return upstream -> upstream
            .compose(groupedResourceInDataset())
            .compose(innerTransform)
            .flatMap(FlowOfRdfNodesInDatasetsOps::ungrouperResourceInDataset);
    }
}
