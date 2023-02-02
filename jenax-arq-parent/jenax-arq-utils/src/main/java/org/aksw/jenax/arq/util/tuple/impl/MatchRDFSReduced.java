package org.aksw.jenax.arq.util.tuple.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

/**
 * RDFS stream reasoner engine that builds upon Jena's {@link MatchRDFS} but handles
 * the X_ANY_ANY and ANY_ANY_ANY cases differently in order to produce fewer duplicates.
 */
public class MatchRDFSReduced<D, C>
    extends TupleFinder3Wrapper<D, C, TupleFinder3<D, C>>
{
    protected CxtInf<C, D> cxtInf;

    /** The non-inferencing backend. Should be the base's base.
     * Note that the delegate of this wrapper backs the backend with RDFS inferences.
     * */
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

    /**
     * This attribute controls the strategy for retrieval of a resource's unique set of properties that is relevant
     * for the RDFS T-BOX.
     * Initially, an attempt is made to iterate all of a resource's triples.
     * Once more than (enumerateThresholdFactor * #properties_with_ranges_in_ontology) triples have been iterated,
     * the strategy is changed to enumeration of the remaining ontology's properties and testing for which of them are linked to
     * the resource.
     */
    float enumerationThresholdFactor = 30;

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

    // Ideally these methods would be part of some context
    protected boolean isTerm(C c) {
        return !isAny(c);
    }

    protected boolean isAny(C c) {
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

    public class Worker_S_ANY_ANY {
        protected ConfigRDFS<C> setup = cxtInf.setup;

        /** The components of the match-pattern */
        protected C ms, mp, mo;

        // The cache of tuples that were inferred for a given subject
        protected Cache<C, Set<C>> seenTypesCache = CacheBuilder.newBuilder().maximumSize(10_000).build();
        protected Cache<C, Set<C>> seenOutPredicatesCache = CacheBuilder.newBuilder().maximumSize(10_000).build();
        protected Cache<Tuple2<C>, Set<C>> seenLinksCache = CacheBuilder.newBuilder().maximumSize(100_000).build();

        protected Set<C> inPredicateCands;
        public long enumerationThreshold;

        public Worker_S_ANY_ANY(C ms) {
            super();
            this.ms = ms;

            inPredicateCands = setup.getPropertyRanges().keySet();
            enumerationThreshold = (long)(inPredicateCands.size() * enumerationThresholdFactor);
        }

        public Stream<D> find() {
            // System.out.println("Find request with " + ms + " - " + mp + " - " + mo);

            // Using Iter.flatMap; Stream.flatMap.iterator results in non-streaming iterators at least on some jvms
            return Iter.asStream(IterUtils.iter(backend.find(ms, mp , mo)).flatMap(this::inf));
        }

        protected D tuple(C s, C p, C o) {
            return getTupleBridge().build(s, p, o);
        }

        /** Resulting iterator typically includes the input tuple - unless it is known to have already been produced */
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
                    // If the match subject (ms) is concrete we have to check incoming edges immediately
                    // Otherwise, only do the work if alwaysFetchRangeTypesBySubject is enabled
                    if (alwaysFetchRangeTypesBySubject || isTerm(ms)) {
                        newInfTypes = accRangeTypesForSubject(newInfTypes, s, seenTypes);
                    }
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
            Set<C> seenInPredicates = getInPredicates(s);
            for (C inP : seenInPredicates) {
                Set<C> rangeTypes = setup.getRange(inP);
                newInfTypes = accTypes(newInfTypes, rangeTypes, seenTypes);
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

        protected Set<C> getInPredicates(C s) {
            Set<C> result;
            if (backend.contains(cxtInf.ANY, cxtInf.rdfType, s)) {
                // Do not fetch incoming predicates for things that are probably classes - i.e. x which appear as ?_type_x
                // Classes may have millions+ incoming properties
                result = Collections.emptySet();
            } else {
                result = getPredicates(backend, s, false, cxtInf.ANY, enumerationThreshold, inPredicateCands);
            }
            return result;
        }
    }

    public static <T> Set<T> addAndGetNew(Set<T> acc, Set<T> base, T addition) {
        return addAndGetNew(acc, base, Collections.singleton(addition));
    }

    /**
     * Add every item in 'additions' that is not in 'base' both to 'base' and 'acc'.
     * If there is a change and 'acc' is null then a fresh linked hash set is allocated.
     * Returns the latest state of 'acc'.
     */
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

    /**
     * Generic method to get a listing of a resource's predicates w.r.t. a given set of relevant predicates.
     * Starts off with iterating the resource's triples (in or out). Once more then 'threshold' triples are seen that way,
     * the presence of the remaining predicates in 'enumeration' is checked directly with contains checks.
     */
    public static <D, C> Set<C> getPredicates(
            TupleFinder3<D, C> backend,
            C s, boolean isForward, C any, long enumerationThreshold, Set<C> enumeration) {
        Set<C> result = new LinkedHashSet<>();
        boolean seenAll = false;

        // The maximum number of predicates we can expect based on the ontology
        int maxSeeableSize = enumeration.size();

        if (enumerationThreshold > 0) {
            Iterator<C> it = (isForward
                        ? backend.find(s, any, any)
                        : backend.find(any, any, s))
                    .map(tuple -> backend.getTupleBridge().get(tuple, 1))
                    .iterator();

            long counter = 0;
            try {
                boolean aborted = false;
                while (it.hasNext()) {
                    C p = it.next();
                    result.add(p);
                    ++counter;
                    if (counter > enumerationThreshold) {
                        aborted = true;
                        break;
                    }
                    if (result.size() >= maxSeeableSize) {
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
            // Don't re-check predicates which we have already seen
            Set<C> remainingCands = new HashSet<>(Sets.difference(enumeration, result));
            for (C candP : remainingCands) {
                boolean isPresent = isForward
                        ? backend.contains(s, candP, any)
                        : backend.contains(any, candP, s);
                if (isPresent) {
                    result.add(candP);
                }
            }
        }
        return result;
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
