package org.aksw.jenax.io.json.writer;

import java.io.IOException;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

public interface RdfObjectNotationWriter
    extends ObjectNotationWriterExt<P_Path0, Node, RdfObjectNotationWriter>
{
    RdfObjectNotationWriter nullValue() throws IOException;
}
