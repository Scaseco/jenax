package org.aksw.jenax.arq.util.exec.query;

import java.util.stream.Stream;

import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDisjunction;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpLateral;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.engine.binding.Binding;

public interface OpExecutorStream {
    Stream<Binding> execute(OpBGP op, Stream<Binding> input);
    Stream<Binding> execute(OpProject op, Stream<Binding> input);

    Stream<Binding> execute(OpGroup op, Stream<Binding> input);
    Stream<Binding> execute(OpOrder op, Stream<Binding> input);
    Stream<Binding> execute(OpExtend op, Stream<Binding> input);

    Stream<Binding> execute(OpService op, Stream<Binding> input);
    Stream<Binding> execute(OpUnion op, Stream<Binding> input);
    Stream<Binding> execute(OpDistinct op, Stream<Binding> input);
    Stream<Binding> execute(OpReduced op, Stream<Binding> input);
    Stream<Binding> execute(OpFilter op, Stream<Binding> input);
    Stream<Binding> execute(OpSlice op, Stream<Binding> input);
    Stream<Binding> execute(OpJoin op, Stream<Binding> input);
    Stream<Binding> execute(OpLateral op, Stream<Binding> input);
    // Stream<Binding> execute(OpVar op, Stream<Binding> input);
    Stream<Binding> execute(OpDisjunction op, Stream<Binding> input);

  /* Interface definition is not yet complete */
}
