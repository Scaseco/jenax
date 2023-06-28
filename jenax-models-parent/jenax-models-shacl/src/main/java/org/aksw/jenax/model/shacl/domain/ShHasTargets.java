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
    @Iri(ShaclTerms.target)
    Set<Resource> getTargets();

    @Iri(ShaclTerms.targetNode)
    Set<RDFNode> getTargetNodes();

    @Iri(ShaclTerms.targetClass)
    Set<RDFNode> getTargetClasses();

    @Iri(ShaclTerms.targetSubjectsOf)
    Set<RDFNode> getTargetSubjectsOf();

    @Iri(ShaclTerms.targetObjectsOf)
    Set<RDFNode> getTargetObjectsOf();
}
