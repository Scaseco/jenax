package org.aksw.jenax.arq.util.tuple.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
import org.apache.jena.graph.Node;
import org.apache.jena.rdfs.engine.CxtInf;
import org.apache.jena.rdfs.engine.MapperX;
import org.apache.jena.rdfs.engine.MatchRDFS;
import org.apache.jena.rdfs.setup.ConfigRDFS;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;

public class MatchRDFSReduced<D, C>
    extends TupleFinder3Wrapper<D, C, TupleFinder3<D, C>>
{
    protected CxtInf<C, D> cxtInf;

    /** The non-inferencing backend. Note that the delegate of this wrapper backs the backend with RDFS inferences */
    protected TupleFinder3<D, C> backend;

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

        if (cxtInf.setup.hasRDFS() && isAny(p) && isAny(o)) {
            // Handle X_??_?? and ??_??_??
            // Create a worker for the find request. The worker uses request-scoped caches in a best-effort attempt
            // to reduce duplicates
            Worker worker = new Worker(s, p, o);
            result = worker.find();
        } else {
            // Delegate to the Jena's original MatchRDFS class (via a TupleFinder adapter)
            result = base.find(s, p, o);
        }
        return result;
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

    class Worker {
        ConfigRDFS<C> setup = cxtInf.setup;

        C ms, mp, mo;

        // The cache of tuples that were inferred for a given subject
        Cache<C, Set<C>> seenTypesCache = CacheBuilder.newBuilder().maximumSize(10_000).build();
        Cache<C, Set<C>> seenOutPredicatesCache = CacheBuilder.newBuilder().maximumSize(10_000).build();
        // Cache<C, Set<C>> seenInPredicatesCache = CacheBuilder.newBuilder().maximumSize(10_000).build();
        Cache<Tuple2<C>, Set<C>> seenLinksCache = CacheBuilder.newBuilder().maximumSize(100_000).build();;

        /**
         * In principle we could handle ranges for ANY_ANY_ANY in a fully streaming way
         * based on the object position. However, if the base stream was grouped by subject
         * then we would break that grouping.
         * Grouping by subject however requires separate requests
         */
        boolean groupBySubject = false;

        /**
         * Control the strategy for generating a resource's set of incoming properties in order to infer types based on the ranges.
         * If false, all incoming properties of a subject will be queried. This is slow if there are many resources with incoming properties in data.
         * If true, all properties with range declarations will be enumerated. This is slow if there are many such properties in the ontology.
         */
        boolean enumerateRanges = false;

        public Worker(C ms, C mp, C mo) {
            super();
            this.ms = ms;
            this.mp = mp;
            this.mo = mo;
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
            C p = getTupleBridge().get(tuple, 1);
            C o = getTupleBridge().get(tuple, 2);

            boolean hasSeenSubject = CacheUtils.getIfPresent(seenTypesCache, s) != null;

            Set<C> seenTypes = CacheUtils.get(seenTypesCache, s, () -> new HashSet<>());
            Set<C> seenOutPredicates = CacheUtils.get(seenOutPredicatesCache, s, () -> new HashSet<>());

            boolean isNewOutPredicate = !seenOutPredicates.contains(p);
            if (isNewOutPredicate) {
                seenOutPredicates.add(p);
            }

            Iterator<D> inferences = null;

            // Expansion for incoming predicates based on rdfs:range (based on the subject)
            if (setup.hasRangeDeclarations()) {
                // If the subject is concrete we have to check incoming edges immediately
                if (groupBySubject || isTerm(s)) {
                    if (!hasSeenSubject) {
                        // System.out.println(s);

                        // The "getInPredicates" method may reject the request for certain subject resources, such
                        // as ones that appear as the objects of rdf:type (i.e. classes)
                        Set<C> newlyInferredTypes = null;
                        Set<C> inPredicates;
                        if (enumerateRanges) {
                            Set<C> inPredicateCands = setup.getPropertyRanges().keySet();
                            inPredicates = inPredicateCands.stream()
                                    .filter(inP -> backend.contains(cxtInf.ANY, inP, s))
                                    .collect(Collectors.toCollection(LinkedHashSet::new));
                        } else {
                            inPredicates = getInPredicates(s);
                        }
                        for (C inP : inPredicates) {
                            Set<C> rangeTypes = setup.getRange(inP);
                            for (C rangeType : rangeTypes) {
                                if (!seenTypes.contains(rangeType)) {
                                    Set<C> rangeTypeClosure = setup.getSuperClassesInc(rangeType);
                                    newlyInferredTypes = addAndGetNew(newlyInferredTypes, seenTypes, rangeTypeClosure);
                                }
                            }
                        }
                        if (newlyInferredTypes != null) {
                            inferences = IterUtils.getOrConcat(inferences,
                                    Iter.iter(newlyInferredTypes).map(t -> tuple(s, cxtInf.rdfType, t)));
                        }
                    }
                } // else { // if (!groupBySubject)
                    // Expansion for rdfs:range (based on the object)
                    // The match object must be any or s

                // The object may not appear as a subject so this might be the last time that we see it
                if (isAny(mo) || Objects.equals(mo, s)) {
                    Node oNode = cxtInf.mapper.toNode(o); // Not ideal using node here; breaks abstraction
                    if (!oNode.isLiteral()) {
                        Set<C> rangeTypes = setup.getRange(p);
                        if (!rangeTypes.isEmpty()) {
                            // If the object appears as a subject we don't have to produce the inferences now
                            boolean appearsAsSubject = backend.contains(o, cxtInf.ANY, cxtInf.ANY);
                            if (!appearsAsSubject) {
                                Set<C> seenObjectTypes = CacheUtils.get(seenTypesCache, o, HashSet::new);
                                Set<C> newlyInferredObjectTypes = null;
                                for (C rangeType : rangeTypes) {
                                    if (!seenObjectTypes.contains(rangeType)) {
                                        Set<C> rangeTypeClosure = setup.getSuperClassesInc(rangeType);
                                        newlyInferredObjectTypes = addAndGetNew(newlyInferredObjectTypes, seenObjectTypes, rangeTypeClosure);
                                    }
                                }
                                if (newlyInferredObjectTypes != null) {
                                    inferences = IterUtils.getOrConcat(inferences,
                                            Iter.iter(newlyInferredObjectTypes).map(t -> tuple(o, cxtInf.rdfType, t)));
                                }
                            }
                        }
                    }
                }
            }

            if (setup.hasClassDeclarations()) {
                // Expansion for rdf:type based on rdfs:subClassOf
                if (cxtInf.rdfType.equals(p) && !seenTypes.contains(o)) {
                    Set<C> superClasses = setup.getSuperClassesInc(o);
                    Set<C> newlyInferredTypes = addAndGetNew(null, seenTypes, superClasses);
                    if (newlyInferredTypes != null) {
                        if (!newlyInferredTypes.isEmpty()) {
                            inferences = IterUtils.getOrConcat(inferences,
                                    Iter.iter(newlyInferredTypes).map(t -> tuple(s, cxtInf.rdfType, t)));
                        }
                    }
                }
            }

            if (setup.hasDomainDeclarations()) {
                // Expansion for any newly seen predicate based on domain of the property
                if (isNewOutPredicate) {
                    Set<C> domainTypes = setup.getDomain(p);
                    Set<C> newlyInferredTypes = null;
                    for (C domainType : domainTypes) {
                        if (!seenTypes.contains(domainType)) {
                            Set<C> domainTypeClosure = setup.getSuperClassesInc(domainType);
                            newlyInferredTypes = addAndGetNew(newlyInferredTypes, seenTypes, domainTypeClosure);
                        }
                    }
                    if (newlyInferredTypes != null) {
                        inferences = IterUtils.getOrConcat(inferences,
                                Iter.iter(newlyInferredTypes).map(t -> tuple(s, cxtInf.rdfType, t)));
                    }
                }
            }

            // Expansion from rdfs:subPropertyOf
            if (setup.hasPropertyDeclarations()) {
                Set<C> superProperties = setup.getSuperProperties(p);
                if (!superProperties.isEmpty() && !(superProperties.size() == 1 && superProperties.contains(p))) {
                    Set<C> seenLinks = CacheUtils.get(seenLinksCache, TupleFactory.create2(s, o), HashSet::new);
                    if (!seenLinks.contains(p)) {
                        seenLinks.add(p);
                        Set<C> newlyInferredPreds = addAndGetNew(null, seenLinks, superProperties);
                        if (newlyInferredPreds != null) {
                            inferences = IterUtils.getOrConcat(inferences,
                                  Iter.iter(newlyInferredPreds).map(p2 -> tuple(s, p2, o)));
                        }
                    }
                }
            }

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

        protected Set<C> getInPredicates(C s) {
            Set<C> result;

            if (backend.contains(cxtInf.ANY, cxtInf.rdfType, s)) {
                // Do not fetch incoming predicates for things that are probably classes - i.e. x which appear as ?_type_x
                // Classes may have millions+ incoming properties
                result = Collections.emptySet();
            } else {
                result = backend.find(s, cxtInf.ANY, cxtInf.ANY).map(tuple -> base.getTupleBridge().get(tuple, 1)).collect(Collectors.toSet());
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
