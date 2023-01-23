package org.aksw.jena_sparql_api.sparql.ext.number;

import java.util.Iterator;
import java.util.List;
import java.util.stream.LongStream;

import org.aksw.jena_sparql_api.sparql.ext.geosparql.PropFuncArgUtils;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.util.IterLib;

/**
 * Generate (big) integer numbers for the specified range start (inclusive) and end (exclusive). The default increment is 1 but can be optionally set
 * to a different value.
 *
 * Stepping must always be a positive value - 0 or negative values return a QueryIterNull.
 *
 * Examples:
 * <pre>
 * (2, 6) number:range ?value # yields values { 2, 3, 4, 5 }
 * (2, 6, 2) number:range ?value # yields {2, 4 }
 * </pre>
 *
 *
 */
public class PF_Range
    extends PropertyFunctionBase
{
    @Override
    public QueryIterator exec(Binding binding, PropFuncArg argSubject, Node predicate, PropFuncArg argObject,
            ExecutionContext execCxt) {

        // Validate the subject's value
        List<Node> subjects = PropFuncArgUtils.getAsList(argSubject);
        int sn = subjects.size();
        if (sn < 2 || sn > 3) {
            throw new ExprEvalException("Expected 2 or 3 arguments (start, end [, increment], got: " + subjects);
        }

        // Validate the object's value
        List<Node> objects = PropFuncArgUtils.getAsList(argObject);
        int on = objects.size();
        if (on != 1) {
            throw new ExprEvalException("Expected only 1 argument, got: " + objects);
        }

        // TODO Enhance with some generic arithmetic framework so that we can dynamically deal with int / float math
        long start = BindingUtils.getNumber(binding, subjects.get(0)).longValue();
        long end = BindingUtils.getNumber(binding, subjects.get(1)).longValue();
        long step = BindingUtils.tryGetNumber(binding, sn > 2 ? subjects.get(2) : null).map(Number::longValue).orElse(1l);

        long rangeDelta = end - start;

        QueryIterator result;

        long stepDir = Long.signum(step);

        if (stepDir <= 0) {
            result = IterLib.noResults(execCxt);
        } else {

            Node outputNode = objects.get(0);

            Number outputValue = BindingUtils.getNumberNullable(binding, outputNode);
            if (outputValue != null) {
                // Check if the outputValue is part of the sequence
                // start + step * n = output
                // n = (output - start) / step | with n must be integer - this means modulo must be 0
                long tmp = outputValue.longValue();

                boolean isMultipleOfStep = (tmp - start) % step == 0;
                boolean isInRange = rangeDelta >= 0
                        ? tmp >= start && tmp < end
                        : tmp <= start && tmp > end;

                result = isInRange && isMultipleOfStep
                    ? IterLib.result(binding, execCxt)
                    : IterLib.noResults(execCxt);

            } else {
                Var outputVar = (Var)outputNode;
                LongStream base = rangeDelta >= 0
                        ? LongStream.iterate(start, x -> x < end, x -> x + step)
                        : LongStream.iterate(start, x -> x > end, x -> x - step);

                Iterator<Binding> it = base
                        .mapToObj(v -> BindingFactory.binding(binding, outputVar, NodeValue.makeInteger(v).asNode()))
                        .iterator();
                result = QueryIterPlainWrapper.create(it, execCxt);
            }
        }

        return result;
    }
}
