package org.aksw.jenax.connection.update;

import java.util.function.BiPredicate;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.modify.UpdateEngine;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.util.Context;

/**
 * Functional interface that matches the actual factory method of {@link UpdateEngineFactory}
 * Eases creation of UpdateEngineFactories from lambdas.
 */
@FunctionalInterface
public interface UpdateEngineFactoryCore {
    UpdateEngine create(DatasetGraph datasetGraph, Binding inputBinding, Context context);

    default UpdateEngineFactory asFactory() {
        return asFactory((datasetGraph, context) -> true);
    }

    default UpdateEngineFactory asFactory(BiPredicate<DatasetGraph, Context> condition) {
        return new UpdateEngineFactory() {
            @Override
            public UpdateEngine create(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
                UpdateEngine result = UpdateEngineFactoryCore.this.create(datasetGraph, inputBinding, context);
                return result;
            }

            @Override
            public boolean accept(DatasetGraph datasetGraph, Context context) {
                boolean result = condition.test(datasetGraph, context);
                return result;
            }
        };
    }
}
