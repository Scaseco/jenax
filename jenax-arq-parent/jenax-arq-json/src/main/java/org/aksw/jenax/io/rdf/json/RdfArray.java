package org.aksw.jenax.io.rdf.json;

public interface RdfArray
    extends RdfElementNode, Iterable<RdfElement>
{
    int size();
    RdfElement get(int index);
    RdfArray add(RdfElement element);
}
