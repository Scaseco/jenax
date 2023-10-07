package org.aksw.facete.v4.api.impl;

import org.aksw.facete.v3.api.FacetedDataQuery;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.facete.treequery2.api.ConstraintNode;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.api.RelationQuery;
import org.aksw.jenax.facete.treequery2.impl.ElementGeneratorLateral;
import org.aksw.jenax.path.core.FacetStep;
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

        ConstraintNode<NodeQuery> node = nq.constraints().fwd(RDFS.label);
        FacetedDataQuery<RDFNode> dataQuery = node.availableValues();

        System.out.println(dataQuery.toConstructQueryNew().getQuery());

        Query query = ElementGeneratorLateral.toQuery(nq);
        System.out.println(query);
    }
}
