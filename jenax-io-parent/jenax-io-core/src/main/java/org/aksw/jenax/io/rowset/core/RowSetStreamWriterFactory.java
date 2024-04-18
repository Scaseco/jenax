package org.aksw.jenax.io.rowset.core;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

import org.apache.jena.sparql.core.Var;

@FunctionalInterface // Only newBuilder() must require implementation
public interface RowSetStreamWriterFactory {
    RowSetStreamWriterBuilder newBuilder();

    // Convenience methods that delegate to the builder

    default RowSetStreamWriter create(OutputStream output, List<Var> vars) {
        return newBuilder().setOutput(output).setVars(vars).build();
    }

    default RowSetStreamWriter create(Writer output, List<Var> vars) {
        return newBuilder().setOutput(output).setVars(vars).build();
    }
}
