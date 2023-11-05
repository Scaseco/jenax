package org.aksw.facete.v3.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetMultiNode;
import org.aksw.facete.v3.api.FacetNodeResource;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.facete.v3.api.FacetedQueryResource;
import org.aksw.facete.v3.bgp.api.BgpDirNode;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.utils.PathAccessorImpl;
import org.aksw.jena_sparql_api.data_query.impl.FacetedQueryGenerator;
import org.aksw.jena_sparql_api.utils.views.map.MapVocab;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.aksw.jenax.sparql.fragment.impl.Fragment3Impl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.expr.E_Bound;
import org.apache.jena.sparql.expr.E_IsBlank;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.Template;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

import com.google.common.collect.ImmutableList;

public class FacetDirNodeImpl
    implements FacetDirNode
{
//	abstract boolean isFwd();
//
//	@Override
//	public FacetDirNode getParent() {
//		// TODO Auto-generated method stub
//		return null;
//	}

    protected FacetNodeResource parent;

    protected BgpDirNode state;

    //protected boolean isFwd;
    //protected Var alias;

    public FacetDirNodeImpl(FacetNodeResource parent, BgpDirNode state) {//boolean isFwd) {
        this.parent = parent;
        //this.isFwd = isFwd;
        this.state = state;
    }

    @Override
    public FacetNodeResource parent() {
        return parent;
    }

    @Override
    public Direction dir() {
        Direction result = state.isFwd() ? Direction.FORWARD : Direction.BACKWARD;
        return result;
    }

    @Override
    public FacetMultiNode via(Resource property, Node component) {
        if (component != null && !FacetStep.isTarget(component)) {
            throw new UnsupportedOperationException("Components not supported yet");
        }

        return new FacetMultiNodeImpl(parent, state.via(property));
        //return new FacetMultiNodeImpl(parent, property, isFwd);
    }


    @Override
    public FacetedDataQuery<RDFNode> facets(boolean includeAbsent) {
        FacetedQueryResource facetedQuery = this.parent().query();

        BgpNode focus = facetedQuery.modelRoot().getFocus();

        BgpNode bgpRoot = facetedQuery.modelRoot().getBgpRoot();

//		BinaryRelation br = FacetedBrowsingSessionImpl.createQueryFacetsAndCounts(path, isReverse, pConstraint);
        FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(bgpRoot));
        Fragment1 baseConcept = query().baseConcept();
        qgen.setBaseConcept(baseConcept);
        facetedQuery.modelRoot().constraints().forEach(c -> qgen.addConstraint(c.expr()));

        Map<String, Fragment3> relations = qgen.getFacetValuesCore(baseConcept, focus, parent.state(), null, null, !this.state.isFwd(), false, false, includeAbsent);

        Fragment1 concept = FacetedQueryGenerator.createConceptFacets(relations, null);

//		UnaryRelation concept = qgen.createConceptFacets(parent.state(), !this.state.isFwd(), false, null);

//		BinaryRelation br = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, pConstraint)(relations, null);
//
//
//		BasicPattern bgp = new BasicPattern();
//		bgp.add(new Triple(br.getSourceVar(), Vocab.facetCount.asNode(), br.getTargetVar()));
//		Template template = new Template(bgp);
//
        FacetedDataQuery<RDFNode> result = new FacetedDataQueryImpl<>(
                parent.query().connection(),
                concept.getElement(),
                concept.getVar(),
                null,
                RDFNode.class);
//
        return result;
    }


//	@Override
//	public DataQuery<FacetCount> facetFocusCounts() {
//		FacetedQueryResource facetedQuery = this.parent().query();
//		BgpNode bgpRoot = facetedQuery.modelRoot().getBgpRoot();
//
//		BgpNode focus = facetedQuery.modelRoot().getFocus();
//
////		BinaryRelation br = FacetedBrowsingSessionImpl.createQueryFacetsAndCounts(path, isReverse, pConstraint);
//		// TODO The API is not consistent with respect to passing the base concept
//		FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(bgpRoot));
//		UnaryRelation baseConcept = query().baseConcept();
//		qgen.setBaseConcept(baseConcept);
//
//		facetedQuery.constraints().forEach(c -> qgen.addConstraint(c.expr()));
//
////		Map<String, BinaryRelation> relations = qgen.createMapFacetsAndValues(focus, parent.state(), !this.state.isFwd(), false, false, includeAbsent);
//		Map<String, TernaryRelation> relations = qgen.getFacetValuesCore(baseConcept, focus, parent.state(), null, null, !this.state.isFwd(), false, false, includeAbsent);
//
//		BinaryRelation br = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, null, includeAbsent);
//
//
//		BasicPattern bgp = new BasicPattern();
//		bgp.add(new Triple(br.getSourceVar(), Vocab.facetCount.asNode(), br.getTargetVar()));
//		Template template = new Template(bgp);
//
//		DataQuery<FacetCount> result = new DataQueryImpl<>(parent.query().connection(), br.getSourceVar(), br.getElement(), template, FacetCount.class);
//
//		return result;
//	}


    @Override
    public FacetedDataQuery<FacetCount> facetCounts(boolean includeAbsent) {
        FacetedDataQuery<FacetCount> result = facetCounts(includeAbsent, false);

        return result;
    }

    @Override
    public FacetedDataQuery<FacetCount> facetFocusCounts(boolean includeAbsent) {
        FacetedDataQuery<FacetCount> result = facetCounts(includeAbsent, true);

        return result;
    }

    public FacetedDataQuery<FacetCount> facetCounts(boolean includeAbsent, boolean focusCount) {
        FacetedQueryResource facetedQuery = this.parent().query();
        BgpNode bgpRoot = facetedQuery.modelRoot().getBgpRoot();

        BgpNode focus = facetedQuery.modelRoot().getFocus();

//		BinaryRelation br = FacetedBrowsingSessionImpl.createQueryFacetsAndCounts(path, isReverse, pConstraint);
        // TODO The API is not consistent with respect to passing the base concept
        FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(bgpRoot));
        Fragment1 baseConcept = query().baseConcept();
        qgen.setBaseConcept(baseConcept);

        facetedQuery.constraints().forEach(c -> qgen.addConstraint(c.expr()));

//		Map<String, BinaryRelation> relations = qgen.createMapFacetsAndValues(focus, parent.state(), !this.state.isFwd(), false, false, includeAbsent);
        Map<String, Fragment3> relations = qgen.getFacetValuesCore(baseConcept, focus, parent.state(), null, null, !this.state.isFwd(), false, false, includeAbsent);

        Fragment2 br = FacetedQueryGenerator.createRelationFacetsAndCounts(relations, null, includeAbsent, focusCount);


        BasicPattern bgp = new BasicPattern();
        bgp.add(new Triple(br.getSourceVar(), Vocab.facetCount.asNode(), br.getTargetVar()));
        Template template = new Template(bgp);

        FacetedDataQuery<FacetCount> result = new FacetedDataQueryImpl<>(
                parent.query().connection(),
                br.getElement(),
                br.getSourceVar(),
                template,
                FacetCount.class);

        return result;
    }

    @Override
    public FacetedDataQuery<FacetValueCount> facetValueTypeCounts() {
        Fragment3 tr = createQueryGenerator()
                .createRelationFacetValueTypeCounts(this.parent().query().focus().state(), this.parent().state(), !this.state.isFwd(), false, null, null, false);
        FacetedDataQuery<FacetValueCount> result = createQueryFacetValueCounts(tr);

        return result;
    }

    @Override
    public FacetedDataQuery<FacetValueCount> facetValueCounts() {
        Fragment3 tr = createQueryGenerator()
                .createRelationFacetValueCounts(this.parent().query().focus().state(), this.parent().state(), !this.state.isFwd(), false, null, null, false);
        FacetedDataQuery<FacetValueCount> result = createQueryFacetValueCounts(tr);

        return result;

//		DataQuery<FacetValueCount> result = createQueryFacetValueCounts(false, false);
//		FacetedQueryResource facetedQuery = this.parent().query();
//
////		BinaryRelation br = FacetedBrowsingSessionImpl.createQueryFacetsAndCounts(path, isReverse, pConstraint);
//		FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(facetedQuery.modelRoot().getBgpRoot()));
//		facetedQuery.constraints().forEach(c -> qgen.addConstraint(c.expr()));
//
//		TernaryRelation tr = qgen.createRelationFacetValues(this.parent().query().focus().state(), this.parent().state(), !this.state.isFwd(), false, null, null);
//
//		// Inject that the object must not be a blank node
//		// TODO There should be a better place to do this - but where?
//		tr = new TernaryRelationImpl(ElementUtils.createElementGroup(ImmutableList.<Element>builder()
//				.addAll(tr.getElements())
//				.add(new ElementFilter(new E_LogicalNot(new E_IsBlank(new ExprVar(tr.getP())))))
//				.build()),
//				tr.getS(),
//				tr.getP(),
//				tr.getO());
//
//
//
//
//
//		BasicPattern bgp = new BasicPattern();
//		bgp.add(new Triple(tr.getS(), Vocab.value.asNode(), tr.getP()));
//		bgp.add(new Triple(tr.getS(), Vocab.facetCount.asNode(), tr.getO()));
//		Template template = new Template(bgp);
//
//		DataQuery<FacetValueCount> result = new DataQueryImpl<>(parent.query().connection(), tr.getS(), tr.getElement(), template, FacetValueCount.class);
//
//		return result;
    }


//	@Override
//	public FacetedQuery query() {
//		FacetedQuery result = parent().query();
//		return result;
//	}

    @Override
    public Fragment2 facetValueRelation() {
        FacetedQueryResource facetedQuery = this.parent().query();

        FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(facetedQuery.modelRoot().getBgpRoot()));
        facetedQuery.constraints().forEach(c -> qgen.addConstraint(c.expr()));

        Fragment3 tr = qgen.createRelationFacetValue(this.parent().query().focus().state(), this.parent().state(), !this.state.isFwd(), null, null, false, false);

        Fragment2 result = new Fragment2Impl(tr.getElement(), tr.getP(), tr.getO());
        return result;
    }


    public FacetedQueryGenerator<BgpNode> createQueryGenerator() {
        FacetedQueryResource facetedQuery = this.parent().query();

//		BinaryRelation br = FacetedBrowsingSessionImpl.createQueryFacetsAndCounts(path, isReverse, pConstraint);
        FacetedQueryGenerator<BgpNode> result = new FacetedQueryGenerator<>(new PathAccessorImpl(facetedQuery.modelRoot().getBgpRoot()));
        result.setBaseConcept(query().baseConcept());
        facetedQuery.constraints().forEach(c -> result.addConstraint(c.expr()));

        return result;
    }

    /**
     * Common method for negated and non-negated facet value counts
     *
     * @param negated
     * @return
     */
    public FacetedDataQuery<FacetValueCount> createQueryFacetValueCounts(Fragment3 tr) { //boolean negated, boolean includeAbsent) {
//		FacetedQueryResource facetedQuery = this.parent().query();
//
////		BinaryRelation br = FacetedBrowsingSessionImpl.createQueryFacetsAndCounts(path, isReverse, pConstraint);
//		FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(facetedQuery.modelRoot().getBgpRoot()));
//		qgen.setBaseConcept(query().baseConcept());
//		facetedQuery.constraints().forEach(c -> qgen.addConstraint(c.expr()));

        //this.query();
        //TernaryRelation tr = relationSupplier.get();//qgen.createRelationFacetValueCounts(this.parent().query().focus().state(), this.parent().state(), !this.state.isFwd(), negated, null, null, includeAbsent);

        // Inject that the object must not be a blank node
        // TODO There should be a better place to do this - but where?

        List<Element> filters = new ArrayList<>();
        boolean discardBlankNodes = false;
        if(discardBlankNodes) {
            filters.add(new ElementFilter(new E_LogicalOr(
                    new E_LogicalNot(new E_Bound(new ExprVar(tr.getP()))),
                    new E_LogicalNot(new E_IsBlank(new ExprVar(tr.getP()))))));
        }

        // NOTE jena's isBlank yields null (type error?) for unbound variables
        // We don't want to filter out blank values but not unbound ones - hence the expression is
        // FILTER(!bound(o) || !blank(?o))
        tr = new Fragment3Impl(ElementUtils.createElementGroup(ImmutableList.<Element>builder()
                .addAll(tr.getElements())
                .addAll(filters)
                .build()),
                tr.getS(),
                tr.getP(),
                tr.getO());





        BasicPattern bgp = new BasicPattern();
        Node superRoot = NodeFactory.createBlankNode();
        bgp.add(new Triple(superRoot, Vocab.predicate.asNode(), tr.getS()));
        bgp.add(new Triple(superRoot, MapVocab.value.asNode(), tr.getP()));
        bgp.add(new Triple(superRoot, Vocab.facetCount.asNode(), tr.getO()));
        Template template = new Template(bgp);

        FacetedDataQuery<FacetValueCount> result = new FacetedDataQueryImpl<>(
                parent.query().connection(),
                tr.getElement(),
                Arrays.asList(tr.getS(), tr.getP()),
                superRoot,
                tr.getS(),
                template,
                FacetValueCount.class);

        return result;
    }

    @Override
    public FacetedDataQuery<FacetValueCount> nonConstrainedFacetValueCounts() {

        Fragment3 tr = createQueryGenerator()
                .createRelationFacetValueCounts(this.parent().query().focus().state(), this.parent().state(), !this.state.isFwd(), true, null, null, false);
        FacetedDataQuery<FacetValueCount> result = createQueryFacetValueCounts(tr);
//
//				true, false);
        return result;
    }

    @Override
    public FacetedDataQuery<FacetValueCount> facetValueCountsWithAbsent(boolean includeAbsent) {
        Fragment3 tr = createQueryGenerator()
                .createRelationFacetValueCounts(this.parent().query().focus().state(), this.parent().state(), !this.state.isFwd(), false, null, null, includeAbsent);
        FacetedDataQuery<FacetValueCount> result = createQueryFacetValueCounts(tr);

        return result;
    }

//	@Override
//	public ExprFragment2 constraintExpr() {
//		FacetedQueryResource facetedQuery = this.parent().query();
//
//		FacetedQueryGenerator<BgpNode> qgen = new FacetedQueryGenerator<>(new PathAccessorImpl(facetedQuery.modelRoot().getBgpRoot()));
//		facetedQuery.constraints().forEach(c -> qgen.addConstraint(c.expr()));
//
//		// TODO Get the constraint expression on that path
//		qgen.getConceptForAtPath(focusPath, facetPath, applySelfConstraints);
//
//		xxx;
//	}

    @Override
    public String toString() {
        Direction dir = dir();
        return (parent == null ? "" : parent.toString()) + (Direction.FORWARD.equals(dir) ? "->" : "<-");
    }

    @Override
    public boolean isFwd() {
        boolean result = state.isFwd();
        return result;
    }
}
