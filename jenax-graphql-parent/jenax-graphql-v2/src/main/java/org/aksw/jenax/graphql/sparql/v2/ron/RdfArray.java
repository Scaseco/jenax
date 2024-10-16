package org.aksw.jenax.graphql.sparql.v2.ron;

public interface RdfArray
    extends RdfElementNode, Iterable<RdfElement>
{
    int size();
    RdfElement get(int index);
    RdfArray add(RdfElement element);

    RdfArray set(int index, RdfElement element);
    RdfArray remove(int index);
}
