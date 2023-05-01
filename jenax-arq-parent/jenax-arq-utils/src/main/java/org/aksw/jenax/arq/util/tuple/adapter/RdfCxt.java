package org.aksw.jenax.arq.util.tuple.adapter;

import java.util.Comparator;

/** Interface for accessing basic RDF term information from an arbitrary representation of it */
public interface RdfCxt<C> {
    boolean isURI(C node);
    boolean isLiteral(C node);
    boolean isBlank(C node);
    boolean isConcrete(C node);
    boolean isVar(C node);
    boolean isNodeTriple(C node);

    /** The comparator for instances of C */
    Comparator<C> comparator();
}
