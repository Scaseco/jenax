package org.aksw.jena_sparql_api_sparql_path2.playground;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.jena.jgrapht.LabeledEdge;
import org.aksw.commons.util.Directed;
import org.aksw.commons.util.triplet.Triplet;
import org.aksw.commons.util.triplet.TripletImpl;
import org.aksw.commons.util.triplet.TripletPath;
import org.aksw.jena_sparql_api.sparql_path2.Nfa;
import org.aksw.jena_sparql_api.sparql_path2.Pair;
import org.aksw.jena_sparql_api.sparql_path2.PathCompiler;
import org.aksw.jena_sparql_api.sparql_path2.PathExecutionUtils;
import org.aksw.jena_sparql_api.sparql_path2.PredicateClass;
import org.aksw.jena_sparql_api.sparql_path2.SparqlKShortestPathFinder;
import org.aksw.jena_sparql_api.sparql_path2.ValueSet;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.path.Path;

public class SparqlKShortestPathFinderYen
    implements SparqlKShortestPathFinder
{
    protected SparqlQueryConnection qef;
    protected int resourceBatchSize;

    public SparqlKShortestPathFinderYen(SparqlQueryConnection qef, int resourceBatchSize) {
        this.qef = qef;
        this.resourceBatchSize = resourceBatchSize;
    }

    public static <S, V, E> TripletPath<V, Directed<E>> convertPath(TripletPath<? extends Entry<S, V>, Directed<E>> path) {
        List<Triplet<V, Directed<E>>> triplets = path.getTriplets().stream()
                .map(t -> (Triplet<V, Directed<E>>)new TripletImpl<>(t.getSubject().getValue(), t.getPredicate(), t.getObject().getValue()))
                .collect(Collectors.toList());

        TripletPath<V, Directed<E>> result = new TripletPath<>(
                path.getStart().getValue(),
                path.getEnd().getValue(),
                triplets);

        return result;
    }

    @Override
    public Iterator<TripletPath<Node, Directed<Node>>> findPaths(Node start, Node end, Path path, Long k) {
        Nfa<Integer, LabeledEdge<Integer, PredicateClass>> nfa = PathCompiler.compileToNfa(path);

//        Function<Pair<ValueSet<Node>>, LookupService<Node, Set<Triplet<Node, Node>>>> createTripletLookupService =
//                pc -> PathExecutionUtils.createLookupService(qef, pc).partition(resourceBatchSize);

            Function<Pair<ValueSet<Node>>, Function<Iterable<Node>, Map<Node, Set<Triplet<Node, Node>>>>> createTripletLookupService =
                    pc -> f -> PathExecutionUtils.createLookupService(qef, pc).partition(resourceBatchSize).fetchMap(f);


        List<TripletPath<Entry<Integer, Node>, Directed<Node>>> kPaths =
                //.<Integer, LabeledEdge<Integer, PredicateClass>, Node, Node>
                YensKShortestPaths.findPaths(
                      nfa,
                      x -> x.getLabel() == null, //LabeledEdgeImpl::isEpsilon,
                      e -> e.getLabel(),
                      createTripletLookupService,
                      start,
                      end,
                      k == null ? Integer.MAX_VALUE : k.intValue());

        Iterator<TripletPath<Node, Directed<Node>>> result =
                kPaths.stream().map(SparqlKShortestPathFinderYen::convertPath).iterator();

        return result;
    }

}
