package org.aksw.jenax.io.json.accumulator;

import java.util.function.Function;

/**
 *
 * @param <I> Input
 * @param <C> classifier
 */
public class StatefulAccumulator<I, C> {

    protected Function<I, C> classify;

    public void accumulate(I input) {
        // S classifier = classify.apply(input);

    }
}
