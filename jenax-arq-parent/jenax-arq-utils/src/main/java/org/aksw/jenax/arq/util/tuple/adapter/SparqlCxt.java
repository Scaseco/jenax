package org.aksw.jenax.arq.util.tuple.adapter;

public interface SparqlCxt<C>
    extends RdfCxt<C>
{
    boolean isAny(C node);
    boolean isAnyNamedGraph(C node);
    boolean isUnionGraph(C node);

    /** Returns a value for which isAny() returns true */
    C any();
    C anyNamedGraph();
    C unionGraph();
}
