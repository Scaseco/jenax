package org.aksw.facete.v4.api.impl;

import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.jena_sparql_api.data_query.api.QuerySpec;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.impl.ElementGeneratorLateral;
import org.aksw.jenax.facete.treequery2.impl.NodeQueryImpl;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class TestFacetedQuery3 {
    @Test
    public void test_01() {
        // RelationQuery is for building tree-like (graphql-like) projections
        // FacetedRelationQuery is to build faceted queries over a relation
        // The goal is to unify both aspects

        RelationQuery rq = RelationQuery.of(ConceptUtils.createSubjectConcept());
        NodeQuery nq = rq.nodeFor(Vars.s);


        // FacetedRelationQuery frq = FacetedRelationQuery.of(ConceptUtils.createSubjectConcept());
        // FacetedQuery fq = frq.getFacetedQuery();
        nq
            .constraints()
                .getOrCreateChild(FacetStep.fwd(RDF.type.asNode(), "a")).enterConstraints().eq(RDFS.Class).activate().leaveConstraints().getParent()
                .getOrCreateChild(FacetStep.fwd(RDF.type.asNode(), "b")).enterConstraints().eq(OWL.Class).activate().leaveConstraints().getParent();

        // nq.availableValues();

        ConstraintNode<NodeQuery> node = nq.constraints().fwd(RDFS.label);
        FacetedDataQuery<RDFNode> dataQuery = node.availableValues();

        QuerySpec spec = dataQuery.toConstructQueryNew();
        System.out.println(spec);

        Query query = ElementGeneratorLateral.toQuery(nq);
        System.out.println(query);
    }

    @Test
    public void test_02() {
        NodeQuery nq = NodeQueryImpl.newRoot().fwd(RDF.type);

        // TODO Somehow add an API that we can inject relations + custom aggregators (= MappedEntityFragments)

        // Note: so far, relationQuery is never null
        // nq.addEntityFragment()
        // System.out.println(nq.relationQuery());

        Query query = ElementGeneratorLateral.toQuery(nq);
        System.out.println(query);

    }

    @Test
    public void test_03() {
        NodeQuery nq = NodeQueryImpl.newRoot();
        nq.addFragment()
            .setFilterFragment(Concept.createForType(RDF.Property.asNode()))
            .fwd(RDFS.label)
            ;

        nq.addFragment()
            .setFilterFragment(Concept.createForType(RDFS.Resource.asNode()));
        Query query = ElementGeneratorLateral.toQuery(nq);
        System.out.println(query);

    }

}
