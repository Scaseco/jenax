package org.aksw.jenax.arq.util.node;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;

// TODO Replace this interface with a Set view
public interface RDFNodeMatcher<T extends RDFNode> {
    ExtendedIterator<T> match(Model model);
}
