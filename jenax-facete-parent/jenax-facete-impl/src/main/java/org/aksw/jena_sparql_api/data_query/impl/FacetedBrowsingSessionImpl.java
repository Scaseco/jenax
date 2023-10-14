package org.aksw.jena_sparql_api.data_query.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.data_query.api.PathAccessor;
import org.aksw.jena_sparql_api.data_query.api.SPath;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.Fragment3Impl;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Range;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.Tables;

import io.reactivex.rxjava3.core.Flowable;

public class FacetedBrowsingSessionImpl {

    private static final Logger logger = LoggerFactory.getLogger(FacetedBrowsingSessionImpl.class);


    protected RDFConnection conn;
    protected SPath root;

    protected SPath focus;

    protected FacetedQueryGenerator<SPath> queryGenerator;

    public FacetedBrowsingSessionImpl(RDFConnection conn) {
        super();
        this.conn = conn;

        FactoryWithModel<SPath> pathFactory = new FactoryWithModel<>(SPath.class);

        root = pathFactory.get();

        PathAccessor<SPath> pathAccessor = new PathAccessorSPath();
        queryGenerator = new FacetedQueryGenerator<>(pathAccessor);

        this.focus = root;

//		queryGenerator.createMapFacetsAndValues(root, false, false);
    }

    public SPath getRoot() {
        return root;
    }

    /**
     * Returns a flow of mappings from predicate to count.
     * If the count is known, the range will include a single element,
     * otherwise it may be created with .atLeast() or .atMost();
     *
     * @param path
     * @param isReverse
     * @return
     */
//	public Flowable<Entry<Node, Range<Long>>> getFacetsAndCounts(SPath path, boolean isReverse, Concept pConstraint) {
//		BinaryRelation br = createQueryFacetsAndCounts(path, isReverse, pConstraint);
//
//
//		//RelationUtils.attr
//
//		Query query = RelationUtils.createQuery(br);
//
//		logger.info("Requesting facet counts: " + query);
//
//		return ReactiveSparqlUtils.execSelect(() -> conn.query(query))
//			.map(b -> new SimpleEntry<>(b.get(br.getSourceVar()), Range.singleton(((Number)b.get(br.getTargetVar()).getLiteral().getValue()).longValue())));
//	}

    public Fragment2 createQueryFacetsAndCounts(SPath path, boolean isReverse, Concept pConstraint) {
        Map<String, Fragment2> relations = null;
        //		Map<String, BinaryRelation> relations = queryGenerator.createMapFacetsAndValues(path, isReverse, false);

        // Align the relations
        //Relation aligned = FacetedBrowsingSession.align(relations.values(), Arrays.asList(Vars.p, Vars.o));


//		List<Relation> aligned = relations.values().stream()
//				.map(r -> FacetedBrowsingSession.align(r, Arrays.asList(Vars.p, Vars.o)))
//				.collect(Collectors.toList());


//		Var countVar = Var.alloc("__count__");
//		List<Element> elements = relations.values().stream()
//				.map(e -> rename(e, Arrays.asList(Vars.p, Vars.o)))
//				.map(Relation::toBinaryRelation)
//				.map(e -> e.joinOn(e.getSourceVar()).with(pConstraint))
//				.map(e -> groupBy(e, Vars.o, countVar))
//				.map(Relation::getElement)
//				.collect(Collectors.toList());
//
//		Element e = ElementUtils.union(elements);
//
//		BinaryRelation result = new BinaryRelationImpl(e, Vars.p, countVar);

//		BinaryRelation result = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, pConstraint, false);

//		return result;
        //Map<String, TernaryRelation> facetValues = g.getFacetValues(focus, path, false);

        return null;
    }


    public Flowable<Cell<Node, Node, Range<Long>>> getFacetValues(SPath facetPath, boolean isReverse, Concept pFilter, Concept oFilter) {
        Fragment3 tr = createQueryFacetValues(facetPath, isReverse, pFilter, oFilter);
        Query query = tr.toQuery();
//		Query query = RelationUtils.createQuery(tr);

        logger.info("Requesting facet value counts: " + query);


        return SparqlRx.execSelectRaw(() -> conn.query(query))
            .map(b -> Tables.immutableCell(b.get(tr.getS()), b.get(tr.getP()), Range.singleton(((Number)b.get(tr.getO()).getLiteral().getValue()).longValue())));

    }

    public Fragment3 createQueryFacetValues(SPath facetPath, boolean isReverse, Concept pFilter, Concept oFilter) {

        Map<String, Fragment3> facetValues = queryGenerator.getFacetValuesCore(null, focus, facetPath, pFilter, oFilter, isReverse, false, false, false);

        Var countVar = Vars.c;
        List<Element> elements = facetValues.values().stream()
                .map(e -> FragmentUtils.rename(e, Arrays.asList(Vars.s, Vars.p, Vars.o)))
                .map(Fragment::toFragment3)
                .map(e -> e.joinOn(e.getP()).with(pFilter))
                .map(e -> FragmentUtils.groupBy(e, Vars.s, countVar, false))
                .map(Fragment::getElement)
                .collect(Collectors.toList());


        Element e = ElementUtils.unionIfNeeded(elements);

        Fragment3 result = new Fragment3Impl(e, Vars.p, Vars.o, countVar);



        return result;

//		FacetedBrowsingSession.align(r, Arrays.asList(Vars.s, Vars.p, Vars.o)
//		List<Relation> aligned = facetValues.values().stream()
//		.map(r -> ))
//		.collect(Collectors.toList());



//		Map<String, TernaryRelation> map = facetValues.entrySet().stream()
//		.collect(Collectors.toMap(Entry::getKey, e -> FacetedQueryGenerator.countFacetValues(e.getValue(), -1)));

    }

    /**
     * TODO How to do combined filters such as "label must match a regex pattern and the count < 1000"?
     *
     * @param filter
     */
//	public getFacetValuesAndCounts(Concept filter) {
//		Map<String, TernaryRelation> map = facetValues.entrySet().stream()
//		.collect(Collectors.toMap(Entry::getKey, e -> FacetedQueryGenerator.countFacetValues(e.getValue(), -1)));
//
//	}
//
//
//	// TODO Move to concept utils
//	public static valuesToFilter() {
//
//	}
}
