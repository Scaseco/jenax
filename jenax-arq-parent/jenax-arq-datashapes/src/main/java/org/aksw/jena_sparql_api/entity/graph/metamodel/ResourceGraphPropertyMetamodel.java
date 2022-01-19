package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Map;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.KeyIri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ResourceGraphPropertyMetamodel
    extends Resource
{
    @IriNs("eg")
    @KeyIri("http://www.example.org/predicate")
    Map<Node, PredicateStats> getStats();


//	@HashId
//	@Inverse
//	@Iri("")
//	ResourceGraphMetamodel getParent();
//
//    @IriNs("eg")
//    Long getDistinctValueCount();
//    GraphPredicateStats setDistinctValueCount(Long count);
//
//    @IriNs("eg")
//    Boolean isDistinctValueCountMinimum();
//    GraphPredicateStats setDistinctValueCountMinimum(Boolean noOrYes);

}
