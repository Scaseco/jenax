package org.aksw.jena_sparql_api.entity.graph.metamodel;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface PredicateStats
    extends Resource
{
    @HashId
    @Inverse
    @Iri("http://www.example.org/predicateStats")
    RGDMetamodel getResourceGraphDirectionMetamodel();

    @HashId
    @IriType
    @Iri("http://www.example.org/predicate")
    String getPredicateIri();

    @Iri("http://www.example.org/predicate")
    Node getPredicateNode();


    @IriNs("eg")
    Long getValueCount();
    PredicateStats setValueCount(Long count);



//    boolean isFwd();
//    QualifiedPredicateStats setFwd(boolean noOrYes);
//
//    String getPredicateIri();
//    QualifiedPredicateStats setPredicateIri(String iri);
//
//    Node getPredicate();
//    QualifiedPredicateStats setPredicate(Node node);
//
//
//    String getGraphIri();
//    QualifiedPredicateStats setGraphIri(String iri);
//
//    Node getGraph();
//    QualifiedPredicateStats setGraph(Node node);
//

//    @IriNs("eg")
//    @KeyIri("http://www.example.org/graph")
//    Map<Node, GraphPredicateStats> getGraphToPredicateStats();
//
//    default GraphPredicateStats getOrCreateStats(String key) {
//        return getOrCreateStats(NodeFactory.createURI(key));
//    }
//
//    default GraphPredicateStats getOrCreateStats(Node key) {
//        GraphPredicateStats result = getGraphToPredicateStats()
//                .computeIfAbsent(key, k -> getModel().createResource().as(GraphPredicateStats.class));
//
//        return result;
//    }
//
}
