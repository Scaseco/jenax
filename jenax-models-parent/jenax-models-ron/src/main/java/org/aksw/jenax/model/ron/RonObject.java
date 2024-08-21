package org.aksw.jenax.model.ron;

import java.util.List;

import org.aksw.jenax.annotation.reprogen.IriNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;

// @Namespace("https://w3id.org/aksw/norse#ron.")
@ResourceView
public interface RonObject
    extends RonElement
{
    @IriNs
    RonObject setValue(Node value);
    Node getValue();

    List<RonMember> getMembers();
}
