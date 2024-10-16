package org.aksw.jenax.model.ron;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.Namespace;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;

@Namespace("https://w3id.org/aksw/norse#ron.")
@ResourceView
public interface RonMember {
    @IriNs
    Node getProperty();
    RonMember setProperty(Node property);

    @IriNs
    Boolean isReverse();
    RonMember setReverse(Boolean isReverse);

    @IriNs
    RDFNode getValue();
    RonMember setValue(RDFNode property);
}
