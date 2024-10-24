package org.aksw.jenax.arq.aggregation;

import java.util.function.Function;

import org.aksw.commons.collector.domain.Accumulator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;

public class AccTransform<I, O>
    extends AccTransform2<Binding, FunctionEnv, I, O>
    implements Acc<O>
{
    public AccTransform(Accumulator<Binding, FunctionEnv, I> subAcc, Function<I, O> transform) {
        super(subAcc, transform);
    }
}

//public class AccTransform<I, O>
//    implements Acc<O>
//{
//    private Acc<I> subAcc;
//    private Function<I, O> transform;
//
//    public AccTransform(Acc<I> subAcc, Function<I, O> transform) {
//        this.subAcc = subAcc;
//        this.transform = transform;
//    }
//
//    @Override
//    public void accumulate(Binding binding) {
//        subAcc.accumulate(binding);
//    }
//
//    @Override
//    public O getValue() {
//        I input = subAcc.getValue();
//        O result = transform.apply(input);
//        return result;
//    }
//
//}
