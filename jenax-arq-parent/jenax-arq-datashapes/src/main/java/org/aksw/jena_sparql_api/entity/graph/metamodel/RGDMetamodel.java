package org.aksw.jena_sparql_api.entity.graph.metamodel;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.KeyIri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

@HashId
@ResourceView
public interface RGDMetamodel
    extends Resource
{
    @HashId
    @Inverse
    @Iri("urn:fwd")
    ResourceGraphMetamodel getFwdRef();

    @HashId
    @Inverse
    @Iri("urn:bwd")
    ResourceGraphMetamodel getBwdRef();


    @IriNs("eg")
    Boolean isPredicateComplete();

    @IriNs("eg")
    @KeyIri("http://www.example.org/predicate")
    Map<Node, PredicateStats> getPredicateStats();



    // FIXME HashId lacks feature to descend into map views
    @Iri("http://www.example.org/predicateStats")
    Set<PredicateStats> getStats();


    default Stream<PredicateStats> find(Node p) {
        boolean isPredicateComplete = Optional.ofNullable(isPredicateComplete()).orElse(false);

        Map<Node, PredicateStats> predMap = getPredicateStats();

        Stream<PredicateStats> result = NodeUtils.isNullOrAny(p)
                ? (isPredicateComplete ? predMap.values().stream() : null)
                : Stream.ofNullable(predMap.get(p));

        return result;
    }
}
