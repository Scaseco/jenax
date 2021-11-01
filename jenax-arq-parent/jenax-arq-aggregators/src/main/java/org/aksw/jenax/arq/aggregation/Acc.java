package org.aksw.jenax.arq.aggregation;

import org.aksw.commons.collector.domain.Accumulator;
import org.apache.jena.sparql.engine.binding.Binding;

/**
 * An accumulator similar to that of Jena, however it uses a generic for the
 * value.
 *
 * @author raven
 *
 * @param <T>
 */
public interface Acc<T>
    extends Accumulator<Binding, T>
{
//    public void accumulate(Binding binding);
//
//    T getValue();
}
