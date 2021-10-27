package org.aksw.jena_sparql_api.sparql_path2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.jena.jgrapht.LabeledEdge;
import org.aksw.commons.util.Directed;
import org.aksw.commons.util.triplet.Triplet;
import org.aksw.commons.util.triplet.TripletImpl;
import org.apache.jena.atlas.lib.tuple.Tuple2;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.jgrapht.Graph;

import com.google.common.collect.Multimap;


/**
 *
 * @author raven
 *
 * @param <S> NFA State type
 * @param <T> NFA Transition type
 * @param <V> Data Vertex Type (e.g. jena's Node or RDFNode)
 * @param <E> Data Property Edge Type (e.g. jena's Node or Property)
 */
public class NfaExecutionUtils {

    public static <S, T, G, V, E> boolean collectPaths(Nfa<S, T> nfa, NfaFrontier<S, G, V, E> frontier, Predicate<T> isEpsilon, Function<NestedPath<V, E>, Boolean> pathCallback) {
        boolean isFinished = false;
        Set<S> currentStates = frontier.getCurrentStates();
        for(S state : currentStates) {

            boolean isFinal = isFinalState(nfa, state, isEpsilon);
            if(isFinal) {
                Multimap<G, NestedPath<V, E>> ps = frontier.getPaths(state);
                for(NestedPath<V, E> path : ps.values()) {
                    //MyPath<V, E> rdfPath = path.asSimplePath();
                    isFinished = pathCallback.apply(path);
                    if(isFinished) {
                        break;
                    }
                }
            }

            if(isFinished) {
                break;
            }
        }

        return isFinished;
    }


    public static <S, D, T extends LabeledEdge<S, ? extends Directed<? extends ValueSet<D>>>> Tuple2<ValueSet<D>> extractNextPropertyClasses(Graph<S, T> nfaGraph, Predicate<T> isEpsilon, Set<S> states, boolean reverse) {
        Set<T> transitions = JGraphTUtils.resolveTransitions(nfaGraph, isEpsilon, states, false);

        ValueSet<D> fwd = ValueSet.createEmpty();
        ValueSet<D> bwd = ValueSet.createEmpty();


        for(T transition : transitions) {
            Directed<? extends ValueSet<D>> label = transition.getLabel();
            boolean isReverse = label.isReverse();

            // invert direction if reverse is true
            isReverse = reverse ? !isReverse : isReverse;


            ValueSet<D> valueSet = label.getValue();

            if(isReverse) {
                bwd = bwd.union(valueSet);
            } else {
                fwd = fwd.union(valueSet);
            }
        }

        Tuple2<ValueSet<D>> result = TupleFactory.create2(fwd, bwd);
        return result;
    }

    /**
     * advances the state of the execution. returns false to indicate finished execution
     * @return
     *
     * TODO: We should detect dead states, as to prevent potential cycling in them indefinitely
     */
//    public boolean advance() {
//        boolean isFinished = collectPaths(nfa, frontier, pathCallback);
//        boolean result;
//
//        if(isFinished) {
//            result = false;
//        } else {
//            frontier = advanceFrontier(frontier, nfa, qef, reversePropertyDirection);
//            result = !frontier.isEmpty();
//        }
//
//        return result;
//    }

    //Function<T, Path> transitionToPath,


    /**
     * The getMatchingTriples function takes as input all paths (by some grouping) for a certain nfa state,
     * and yields a set of triplets that connect to the endpoints of the current paths in that group
     *
     *
     * @param frontier
     * @param nfaGraph
     * @param isEpsilon
     * @param getMatchingTriplets
     * @param pathGrouper
     * @param earlyPathReject
     * @return
     */
    public static <S, T, G, V, E> NfaFrontier<S, G, V, E> advanceFrontier(
            NfaFrontier<S, G, V, E> frontier,
            Graph<S, T> nfaGraph,
            Predicate<T> isEpsilon,
            TripletLookup<T, G, V, E> getMatchingTriplets,
            //BiFunction<T, Multimap<G, NestedPath<V, E>>, Map<V, Set<Triplet<V, E>>>> getMatchingTriplets,
            Function<NestedPath<V, E>, G> pathGrouper,
            Predicate<NestedPath<V, E>> earlyPathReject // Function that can reject paths before they are added to the frontier, such as by consulting a join summary or performing a reachability test to the target
            ) {
        // Prepare the next frontier
        NfaFrontier<S, G, V, E> result = new NfaFrontier<S, G, V, E>();

        Set<S> currentStates = frontier.getCurrentStates();
        for(S state : currentStates) {
            Multimap<G, NestedPath<V, E>> ps = frontier.getPaths(state);
            Set<T> transitions = JGraphTUtils.resolveTransitions(nfaGraph, isEpsilon, state, false);

            for(T trans : transitions) {

                Map<V, Set<Triplet<V, E>>> vToTriplets = getMatchingTriplets.lookup(trans, ps);
                Collection<NestedPath<V, E>> allPaths = ps.values();

                for(NestedPath<V, E> parentPath : allPaths) {
                    V node = parentPath.getCurrent();

                    Set<Triplet<V, E>> triplets = vToTriplets.getOrDefault(node, Collections.emptySet());

                    for(Triplet<V, E> t : triplets) {
                        E p = t.getPredicate();
                        Directed<E> p0;

                        V o;
                        if(t.getSubject().equals(node)) {
                            p0 = new Directed<E>(p, false);
                            o = t.getObject();
                        } else if(t.getObject().equals(node)) {
                            p0 = new Directed<E>(p, true);
                            o = t.getSubject();
                        } else {
                            throw new RuntimeException("Should not happen");
                        }

                        NestedPath<V, E> next = new NestedPath<V, E>(new ParentLink<V, E>(parentPath, p0), o);
                        G groupKey = pathGrouper.apply(next);

                        if(next.isCycleFree()) {
                            boolean reject = earlyPathReject.test(next);
                            if(!reject) {
                                S targetState = nfaGraph.getEdgeTarget(trans);
                                result.add(targetState, groupKey, next);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }


    /**
     * Convenience method.
     *
     * Returns true if the given path can reach any of the target vertices
     * under a given edge.
     *
     * TODO We need to create a join summary excerpt
     *
     */
//    public static boolean <V, E> isReachableUnder(Graph<V, E> graph, NestedPath<V, E> path, E underEdge, Set<V> targets) {
//        JGraphTUtils.getAllPaths(graph, start, end)
//    }
//
//    public static <V, E> isReachable(Set<V> startVertices, Set<V> targetVertices) {
//    //    JGraphTUtils.
//    }


    /**
     * Tests if a state is final. This includes if there is a transitive
     * connection via epsilon edges to a final state.
     *
     * @param state
     * @return
     */
    public static <S, T> boolean isFinalState(Nfa<S, T> nfa, S state, Predicate<T> isEpsilon) {
        Graph<S, T> graph = nfa.getGraph();
        Set<S> endStates = nfa.getEndStates();
        Set<S> reachableStates = JGraphTUtils.transitiveGet(graph, state, 1, x -> isEpsilon.test(x));
        boolean result = reachableStates.stream().anyMatch(s -> endStates.contains(s));
        return result;
    }

    public static <S, T> boolean isStartState(Nfa<S, T> nfa, S state, Predicate<T> isEpsilon) {
        Graph<S, T> graph = nfa.getGraph();
        Set<S> startStates = nfa.getStartStates();
        Set<S> reachableStates = JGraphTUtils.transitiveGet(graph, state, -1, x -> isEpsilon.test(x));
        boolean result = reachableStates.stream().anyMatch(s -> startStates.contains(s));
        return result;
    }


    /**
     * Given
     * - an nfa and
     * - join graph, determine for a given
     * - predicate (pointing either forwards or backwards) in a certain set of     //nestedPath in a certain set of
     * - nfa states of whether it can reach the
     * - set of predicates leading to the target states.
     *
     * Execution works as follows:
     * It is assumed that the given predicate is reachable, so no further checks are performed.
     *
     *
     * BiFunction<Set<V>, Directed<T>, Map<V, Set<Triplet<V, E>>>> getMatchingTriplets
     */

    public static <S, T, P, Q> boolean isTargetReachable(Nfa<S, T> nfa, Predicate<T> isEpsilon, Set<S> states, BiPredicate<Directed<T>, Q> matcher, Graph<P, Q> joinGraph, Directed<P> diPredicate, Tuple2<Set<P>> targetPreds) {
        // Return true if there is at least 1 path
        return false;
    }


    /**
     * matcher(Directed<T>, joinGraph, vertex)
     *
     * @param nfa
     * @param isEpsilon
     * @param states
     * @param matcher
     * @param joinGraph
     * @param diPredicate
     * @param targetPreds
     * @return
     */
//T extends Pair<ValueSet<V>>
    public static <S, T, P, Q> List<NestedPath<P, Q>> findPathsInJoinSummary(
            Nfa<S, T> nfa,
            Predicate<T> isEpsilon,
            Set<S> states,
            Graph<P, Q> joinGraph,
            P startVertex, // the start vertex
            Long k,
            BiFunction<T, P, Set<Directed<P>>> initPred,  //Triplet<P, Q>>
            BiFunction<T, Directed<P>, Set<Directed<P>>> transAndNodesToTriplets,  //Triplet<P, Q>>
            Function<NestedPath<P, Q>, Boolean> pathCallback) {


        // Group by the directed of the prior predicate (null if there is no prior predicate)
        Function<NestedPath<P, Q>, Directed<P>> pathGrouper = nestedPath ->
            nestedPath.getParentLink().map(pl ->
                new Directed<P>(nestedPath.getCurrent(), pl.getDiProperty().isReverse())
            ).orElse(null);

        List<NestedPath<P, Q>> result = new ArrayList<>();
        NfaExecutionUtils.executeNfa(
                nfa,
                states,
                isEpsilon,
                Collections.singleton(startVertex),
                pathGrouper,
                (trans, diPredToPaths) -> {
                    // there is a transition, an there is our initial predicate,
                    // and we now need to determine successor triplet of this predicate in regard to the transition
                    Map<P, Set<Triplet<P, Q>>> r = new HashMap<>();

                    //Set<Directed<P>> diPreds = diPredToPaths.keySet();
                    //for(Directed<P> diPred : diPreds) {
                    for(Entry<Directed<P>, Collection<NestedPath<P, Q>>> entry : diPredToPaths.asMap().entrySet()) {
                        Directed<P> diPred = entry.getKey();

                        if(diPred == null) {
                            Collection<NestedPath<P, Q>> paths = entry.getValue();
                            paths.stream().forEach(path -> {
                                P n = path.getCurrent();
                                Set<Directed<P>> succ = initPred.apply(trans, path.getCurrent());

                                Set<Triplet<P, Q>> triplets = succ.stream()
                                        .map(dp -> TripletImpl.create(n, (Q)null, dp.getValue(), dp.isReverse()))
                                        .collect(Collectors.toSet());

                                r.put(n, triplets);
                            });
                        } else {
                            P pred = diPred.getValue();

                            Set<Directed<P>> nextDiPreds = transAndNodesToTriplets.apply(trans, diPred);

                            Set<Triplet<P, Q>> triplets = nextDiPreds.stream()
                                    .map(dp -> TripletImpl.create(pred, (Q)null, dp.getValue(), dp.isReverse())) // TODO get rid of the null - maybe: joinGraph.getEdge(pred, ...)
                                    .collect(Collectors.toSet());

                            r.put(pred, triplets);
                        }
                    }

                    return r;
                }, nestedPath -> {
                    boolean accept = pathCallback.apply(nestedPath);
                    if(accept) {
                        result.add(nestedPath);
                    }

                    boolean abort = k != null && result.size() >= k;
                    return abort;
                });



//        JGraphTUtils.getAllPaths(graph, starts, ends)
//
        return result;
    }


    /**
     * Generic Nfa execution
     *
     * @param nfa
     * @param startStates
     * @param isEpsilon
     * @param startVertices
     * @param pathGrouper
     * @param getMatchingTriplets
     * @param pathCallback
     */
    public static <S, T, G, V, E> void executeNfa(
            Nfa<S, T> nfa,
            Set<S> startStates,
            Predicate<T> isEpsilon,
            Set<V> startVertices,
            Function<NestedPath<V, E>, G> pathGrouper,
            TripletLookup<T, G, V, E> getMatchingTriplets,
            //BiFunction<T, Multimap<G, NestedPath<V, E>>, Map<V, Set<Triplet<V, E>>>> getMatchingTriplets,
            Function<NestedPath<V, E>, Boolean> pathCallback) {

        NfaFrontier<S, G, V, E> frontier = new NfaFrontier<>();
        NfaFrontier.addAll(frontier, startStates, pathGrouper, startVertices);

        while(!frontier.isEmpty()) {

            boolean abort = collectPaths(nfa, frontier, isEpsilon, pathCallback);
            if(abort) {
                break;
            }

            Graph<S, T> nfaGraph = nfa.getGraph();

            NfaFrontier<S, G, V, E> nextFrontier = advanceFrontier(
                    frontier,
                    nfaGraph,
                    isEpsilon,
                    getMatchingTriplets,
                    pathGrouper,
                    x -> false);

            frontier = nextFrontier;
        }
    }
}

//
//@FunctionalInterface
//interface NfaDataGraphMatcher<T, V, E> {
//    boolean matches(Directed<T> trans, Graph<V, E> graph, V vertex);
//}

