package org.aksw.jenax.arq.util.tuple.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.tuple.bridge.TupleBridge3;
import org.aksw.commons.tuple.finder.TupleFinder3;
import org.aksw.commons.tuple.finder.TupleFinder3Wrapper;
import org.aksw.commons.util.cache.CacheUtils;
import org.aksw.jenax.arq.util.tuple.IterUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.tuple.Tuple2;
import org.apache.jena.atlas.lib.tuple.TupleFactory;
import org.apache.jena.ext.com.google.common.graph.Traverser;
import org.apache.jena.graph.Node;
import org.apache.jena.rdfs.engine.CxtInf;
import org.apache.jena.rdfs.engine.MapperX;
import org.apache.jena.rdfs.engine.MatchRDFS;
import org.apache.jena.rdfs.setup.ConfigRDFS;

import com.github.jsonldjava.shaded.com.google.common.math.LongMath;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;

public class MatchRDFSReduced<D, C>
    extends TupleFinder3Wrapper<D, C, TupleFinder3<D, C>>
{
    protected CxtInf<C, D> cxtInf;

    /** The non-inferencing backend. Note that the delegate of this wrapper backs the backend with RDFS inferences */
    protected TupleFinder3<D, C> backend;


    /**
     * In principle we could handle ranges for ANY_ANY_ANY in a fully streaming way
     * based on the object position. However, if the base stream was grouped by subject
     * then we would break that grouping.
     * Grouping by subject however requires separate requests
     *
     * Recommended: 'false'
     */
    boolean alwaysFetchRangeTypesBySubject = false;

    /** Fall back to enumeration strategy as soon as (factor * #properties_with_ranges_in_ontology) have been retrieved. */
    float enumerateRatio = 30;

    protected MatchRDFSReduced(TupleFinder3<D, C> matchRDFS, TupleFinder3<D, C> backend, CxtInf<C, D> cxtInf) {
        super(matchRDFS);
        this.cxtInf = cxtInf;
        this.backend = backend;
    }

    public static <D, C> TupleFinder3<D, C> create(ConfigRDFS<C> setup, MapperX<C, D> mapper, TupleFinder3<D, C> backend) {
        // InfFindTuple is also a CxtInf
        InfFindTuple<D, C> matchRDFS = new InfFindTuple<>(setup, mapper, backend);
        return new MatchRDFSReduced<>(matchRDFS, backend, matchRDFS);
    }

    private boolean isTerm(C c) {
        return !isAny(c);
    }

    private boolean isAny(C c) {
        return c == null || cxtInf.ANY.equals(c);
    }

    @Override
    public Stream<D> find(C s, C p, C o) {
        Stream<D> result;

        boolean isRdfType = cxtInf.rdfType.equals(p);

        // If we ask for RDF type and there are domain/range declarations then
        // we need to look at all properties
        //if (cxtInf.setup.hasRDFS() && (isAny(p) || isRdfType) && isAny(o)) {
        if (cxtInf.setup.hasRDFS() && (isAny(p) || isRdfType) && isAny(o)) {
            // Handle X_??_?? and ??_??_??
            // Create a worker for the find request. The worker uses request-scoped caches in a best-effort attempt
            // to reduce duplicates
            Worker_S_ANY_ANY worker = new Worker_S_ANY_ANY(s); // p=cxtInf.ANY, o=cxtInf.ANY);
            result = worker.find();

            if (isRdfType) {
                result = result.filter(t -> cxtInf.rdfType.equals(getTupleBridge().get(t, 1)));
            }

        } else {
            // Delegate to the Jena's original MatchRDFS class (via a TupleFinder adapter)
            result = base.find(s, p, o);
        }
        return result;
    }


    public static <T> Set<T> addAndGetNew(Set<T> acc, Set<T> base, T addition) {
        return addAndGetNew(acc, base, Collections.singleton(addition));
    }

    public static <T> Set<T> addAndGetNew(Set<T> acc, Set<T> base, Set<T> additions) {
        Set<T> result = acc;
        List<T> newItems = new ArrayList<>(Sets.difference(additions, base));
        base.addAll(newItems);
        if (!newItems.isEmpty()) {
            if (result == null) {
                result = new LinkedHashSet<>();
            }
            result.addAll(newItems);
        }
        return result;
    }

//    public static <C, X> Set<C> addAndGetNew(Set<C> acc, Set<C> base, Iterable<X> items, Function<X, Set<C>> itemToAdditions) {
//        for (X item : items) {
//            Set<C> additions = itemToAdditions.apply(item);
//            addAndGetNew(acc, base, additions);
//        }
//        return acc;
//    }

    class Worker_S_ANY_ANY {
        ConfigRDFS<C> setup = cxtInf.setup;

        C ms, mp, mo;

        // The cache of tuples that were inferred for a given subject
        Cache<C, Set<C>> seenTypesCache = CacheBuilder.newBuilder().maximumSize(10_000).build();
        Cache<C, Set<C>> seenOutPredicatesCache = CacheBuilder.newBuilder().maximumSize(10_000).build();
        // Cache<C, Set<C>> seenInPredicatesCache = CacheBuilder.newBuilder().maximumSize(10_000).build();
        Cache<Tuple2<C>, Set<C>> seenLinksCache = CacheBuilder.newBuilder().maximumSize(100_000).build();;


        /**
         * Control the strategy for generating a resource's set of incoming properties.
         * This is needed to infer types based on the ranges.
         * If false, all incoming properties of a subject will be queried. This is slow if there are many resources with incoming properties in data.
         * If true, all properties with range declarations will be enumerated. This is slow if there are many such properties in the ontology.
         *
         * A setting of 'true' has a fixed small (but noticeable) overhead per subject
         * A setting of 'false' has a large overhead for subjects with lots of triples.
         *   Even e.g. a LIMIT 10 may take seconds because of the need to scan all incoming edges.
         *   For this reason the recommended setting is 'true'.
         *
         * With 'false' more consistent response times can be expected however at the cost of less overall performance.
         */
        // boolean enumerateRanges = false;


        protected Set<C> inPredicateCands;

        public long enumerationThreshold;

        public Worker_S_ANY_ANY(C ms) {
            super();
            this.ms = ms;

            Set<C> directInPredicateCands = setup.getPropertyRanges().keySet();
            inPredicateCands = Streams.stream(
                    Traverser.forGraph(setup::getSubProperties)
                    .depthFirstPreOrder(directInPredicateCands)).collect(Collectors.toSet());

            enumerationThreshold = (long)(inPredicateCands.size() * enumerateRatio);
        }

        public Stream<D> find() {
            // System.out.println("Find request with " + ms + " - " + mp + " - " + mo);

            // Using Iter.flatMap; Stream.flatMap.iterator results in non-streaming iterators at least on some jvms
            return Iter.asStream(IterUtils.iter(backend.find(ms, mp , mo)).flatMap(this::inf));
        }

        protected D tuple(C s, C p, C o) {
            return getTupleBridge().build(s, p, o);
        }

        protected Iterator<D> inf(D tuple) {
            C s = getTupleBridge().get(tuple, 0);
            C p = getTupleBridge().get(tuple, 1); // initial predicate
            C o = getTupleBridge().get(tuple, 2);

            boolean hasSeenSubject = CacheUtils.getIfPresent(seenTypesCache, s) != null;

            Set<C> seenTypes = CacheUtils.get(seenTypesCache, s, () -> new HashSet<>());
            Set<C> seenOutPredicates = CacheUtils.get(seenOutPredicatesCache, s, () -> new HashSet<>());

            boolean isNewOutPredicate = !seenOutPredicates.contains(p);
            if (isNewOutPredicate) {
                seenOutPredicates.add(p);
            }

            // Newly inferred types derived from this triple
            Set<C> newInfTypes = null;

            Set<C> superPropertiesInc; // We may have p subPropertyOf rdf:type
            Iterator<D> inferences = null;

            // Expansion from rdfs:subPropertyOf
            if (setup.hasPropertyDeclarations()) {
                superPropertiesInc = setup.getSuperPropertiesInc(p);
                if (superPropertiesInc.isEmpty()) {
                    superPropertiesInc = Collections.singleton(p);
                }
                inferences = withSuperProperties(inferences, superPropertiesInc, s, p, o);
            } else {
                superPropertiesInc = Collections.singleton(p);
            }

            // Expansion for incoming predicates based on rdfs:range (based on the subject)
            if (setup.hasRangeDeclarations()) {
                if (!hasSeenSubject) {
                    newInfTypes = accRangeTypesForSubject(newInfTypes, s, seenTypes);
                }

                inferences = withRangeTypesForObject(inferences, s, p, o);
            }

            if (setup.hasClassDeclarations()) {
                // Expansion for rdf:type based on rdfs:subClassOf
                if (superPropertiesInc.contains(cxtInf.rdfType) && !seenTypes.contains(o)) {
                    newInfTypes = accTypes(newInfTypes, o, seenTypes);
                }
            }

            if (setup.hasDomainDeclarations()) {
                // Expansion for any newly seen predicate based on domain of the property
                Set<C> domainTypes = setup.getDomain(p);
                newInfTypes = accTypes(newInfTypes, domainTypes, seenTypes);
            }

            inferences = withTypeInfs(inferences, s, newInfTypes);

            // Suppress rdf:type triples when the type has already been seen
            boolean isSuppressedTriple = cxtInf.rdfType.equals(p) && seenTypes.contains(o);

            Iterator<D> result;
            if (isSuppressedTriple) {
                result = inferences == null ? Iter.empty() : inferences;
            } else {
                Iterator<D> self = Iter.of(tuple);
                result = inferences == null
                    ? self
                    : Iter.concat(self, inferences);
            }

            return result;
        }

        protected Iterator<D> withTypeInfs(Iterator<D> result, C s, Set<C> newTypes) {
            if (newTypes != null) {
                result = IterUtils.getOrConcat(result, Iter.iter(newTypes).map(t -> tuple(s, cxtInf.rdfType, t)));
            }
            return result;
        }

        public Iterator<D> withSuperProperties(Iterator<D> inferences, Set<C> superPropertiesInc, C s, C p, C o) {
            // Infer super properties (s p2 o) <- (s p1 o) (p1 subPropertyOf p2)
            if (!(superPropertiesInc.size() == 1 && superPropertiesInc.contains(p))) {
                Set<C> seenLinks = CacheUtils.get(seenLinksCache, TupleFactory.create2(s, o), HashSet::new);
                if (!seenLinks.contains(p)) {
                    seenLinks.add(p); // Marking this property seen suppresses inference of the current triple; we will output it at the end
                    Set<C> newlyInferredPreds = addAndGetNew(null, seenLinks, superPropertiesInc);
                    if (newlyInferredPreds != null) {
                        inferences = IterUtils.getOrConcat(inferences,
                              Iter.iter(newlyInferredPreds).map(p2 -> tuple(s, p2, o)));
                    }
                }
            }
            return inferences;
        }

        public Iterator<D> withRangeTypesForObject(Iterator<D> inferences, C s, C p, C o) {
            // The object may not appear as a subject so this might be the last time that we see it
            // In the rare case that o == s and alwaysFetchRangeTypesBySubject the work was already done
            if (isAny(ms) || (Objects.equals(o, s) && !alwaysFetchRangeTypesBySubject)) {
                Node oNode = cxtInf.mapper.toNode(o); // Not ideal forcing materialization to node here
                if (!oNode.isLiteral()) {
                    boolean emitObjectRangeTypesNow = true;
                    if (alwaysFetchRangeTypesBySubject) {
                        // If the object appears as a subject we don't have to produce the inferences now
                        boolean appearsAsSubject = backend.contains(o, cxtInf.ANY, cxtInf.ANY);
                        emitObjectRangeTypesNow = !appearsAsSubject;
                    }

                    if (emitObjectRangeTypesNow) {
                        Set<C> rangeTypes = setup.getRange(p);
                        if (!rangeTypes.isEmpty()) {
                            Set<C> seenObjectTypes = CacheUtils.get(seenTypesCache, o, HashSet::new);
                            Set<C> newlyInferredObjectTypes = accTypes(null, rangeTypes, seenObjectTypes);
                            inferences = withTypeInfs(inferences, o, newlyInferredObjectTypes);
                        }
                    }
                }
            }
            return inferences;
        }

        public Set<C> accRangeTypesForSubject(Set<C> newInfTypes, C s, Set<C> seenTypes) {
            // If the subject is concrete we have to check incoming edges immediately
            if (alwaysFetchRangeTypesBySubject || isTerm(s)) {
                Set<C> seenInPredicates = new LinkedHashSet<>();
                boolean seenAll = false;

                // The maximum number of predicates we can expect based on the ontology
                int maxSeeableSize = inPredicateCands.size();

                if (enumerationThreshold > 0) {
                    Iterator<C> it = getInPredicates(s);
                    long counter = 0;
                    try {
                        boolean aborted = false;
                        while (it.hasNext()) {
                            C inP = it.next();
                            seenInPredicates.add(inP);
                            ++counter;
                            if (counter > enumerationThreshold) {
                                aborted = true;
                                break;
                            }

                            if (seenInPredicates.size() >= maxSeeableSize) {
                                // This case may trigger for ontologies with very few range declarations
                                seenAll = true;
                                break;
                            }
                        }
                        seenAll = !aborted;
                    } finally {
                        Iter.close(it);
                    }
                }

                if (!seenAll) {
                    // Don't check predicates we have already seen
                    Set<C> remainingCands = new HashSet<>(Sets.difference(inPredicateCands, seenInPredicates));
                    for (C candP : remainingCands) {
                        if (backend.contains(cxtInf.ANY, candP, s)) {
                            seenInPredicates.add(candP);
                        }
                    }
                }

                for (C inP : seenInPredicates) {
                    Set<C> rangeTypes = setup.getRange(inP);
                    newInfTypes = accTypes(newInfTypes, rangeTypes, seenTypes);
                }
            }
            return newInfTypes;
        }


        protected Set<C> accTypes(Set<C> result, C directType, Set<C> seenTypes) {
            return accTypes(result, Collections.singleton(directType), seenTypes);
        }

        protected Set<C> accTypes(Set<C> result, Set<C> directTypes, Set<C> seenTypes) {
            for (C directType : directTypes) {
                if (!seenTypes.contains(directType)) {
                    result = addAndGetNew(result, seenTypes, directType);
                    Set<C> typeClosure = setup.getSuperClasses(directType);
                    result = addAndGetNew(result, seenTypes, typeClosure);
                }
            }
            return result;
        }

        protected Iterator<C> getInPredicates(C s) {
            Iterator<C> result;
            if (backend.contains(cxtInf.ANY, cxtInf.rdfType, s)) {
                // Do not fetch incoming predicates for things that are probably classes - i.e. x which appear as ?_type_x
                // Classes may have millions+ incoming properties
                result = Collections.emptyIterator();
            } else {
                result = backend.find(cxtInf.ANY, cxtInf.ANY, s)
                            .map(tuple -> base.getTupleBridge().get(tuple, 1))
                            .iterator();
            }
            return result;
        }
    }

    /** Bridge between TupleFinder and MatchRDF - this class inherits both */
    public static class InfFindTuple<D, C>
        extends MatchRDFS<C, D>
        implements TupleFinder3<D, C>
    {
        private final TupleFinder3<D, C> base;

        public InfFindTuple(ConfigRDFS<C> setup, MapperX<C, D> mapper, TupleFinder3<D, C> backend) {
            super(setup, mapper);
            this.base = backend;
        }

        @Override
        public Stream<D> sourceFind(C s, C p, C o) {
            return base.find(s,p,o);
        }

        @Override
        protected boolean sourceContains(C s, C p, C o) {
            return base.contains(s, p, o);
        }

        @Override
        protected D dstCreate(C s, C p, C o) {
            return base.getTupleBridge().build(s, p, o);
        }

        @Override
        public Stream<D> find(C s, C p, C o) {
            return match(s, p, o);
        }

        @Override
        public TupleBridge3<D, C> getTupleBridge() {
            return base.getTupleBridge();
        }
    }
}
