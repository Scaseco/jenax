package org.aksw.jenax.model.shacl.domain;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface ShHasTargets
    extends Resource
{
    @Iri(ShTerms.target)
    Set<Resource> getTargets();

    @Iri(ShTerms.targetNode)
    Set<RDFNode> getTargetNodes();

    @Iri(ShTerms.targetClass)
    Set<RDFNode> getTargetClasses();

    @Iri(ShTerms.targetSubjectsOf)
    Set<RDFNode> getTargetSubjectsOf();

    @Iri(ShTerms.targetObjectsOf)
    Set<RDFNode> getTargetObjectsOf();
}
