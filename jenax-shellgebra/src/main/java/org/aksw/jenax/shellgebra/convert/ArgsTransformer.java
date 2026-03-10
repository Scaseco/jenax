package org.aksw.jenax.shellgebra.convert;

import java.util.List;

public interface ArgsTransformer {
    /**
     * Process an argument list into a new one.
     */
    List<String> transform(List<String> args);
}
