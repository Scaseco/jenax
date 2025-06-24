package org.aksw.jenax.dataaccess.sparql.linksource.track;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;

public interface PlanWrapper
    extends Plan
{
    Plan getDelegate();

    @Override
    default void output(IndentedWriter out, SerializationContext sCxt) {
        getDelegate().output(out, sCxt);
    }

    @Override
    default String toString(PrefixMapping pmap) {
        return getDelegate().toString(pmap);
    }

    @Override
    default void output(IndentedWriter out) {
        getDelegate().output(out);
    }

    @Override
    default void close() {
        getDelegate().close();
    }

    @Override
    default Op getOp() {
        return getDelegate().getOp();
    }

    @Override
    default QueryIterator iterator() {
        return getDelegate().iterator();
    }

}
