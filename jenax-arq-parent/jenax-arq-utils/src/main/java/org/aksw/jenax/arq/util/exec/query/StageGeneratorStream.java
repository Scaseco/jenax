package org.aksw.jenax.arq.util.exec.query;

import java.util.stream.Stream;

import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;

public interface StageGeneratorStream {
    Stream<Binding> execute(BasicPattern pattern, Stream<Binding> input, ExecutionContext execCxt) ;
}
