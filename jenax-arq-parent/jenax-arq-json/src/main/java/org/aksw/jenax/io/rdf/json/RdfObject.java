package org.aksw.jenax.io.rdf.json;

import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.path.P_Path0;

public interface RdfObject
    extends RdfElementResource
{
    /** Get the members of this object. */
    // XXX We may want to add support for dedicated forward / backward views
    Map<P_Path0, RdfElement> getMembers();

    // RdfObject add(P_Path0, RdfElement value);

    RdfObject addForward(Node property, RdfElement value);
    RdfObject addForward(RDFNode property, RdfElement value);

    RdfObject addBackward(Node property, RdfElement value);
    RdfObject addBackward(RDFNode property, RdfElement value);

    /** Return a mutable sub view of all forward keys that are strings and that can thus be viewed as json objects. */
    // getJsonSubView()
}
