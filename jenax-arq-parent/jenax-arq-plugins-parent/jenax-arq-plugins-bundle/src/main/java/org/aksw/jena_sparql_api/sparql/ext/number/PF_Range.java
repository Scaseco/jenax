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
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;

/**
 * Generate (big) integer numbers for the specified range start (inclusive) and end (exclusive). The default increment is 1 but can be optionally set
 * to a different value.
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

        QueryIterator result;

        // Catch the case where the sign of the step differs from that of (end - start)
        // Attempting to fetch e.g. the range [0, 10) with an increment of -1 would otherwise hang indefinitely
        int rangeDir = Long.signum(end - start);
        long stepDir = Long.signum(step);

        if (rangeDir != stepDir) {
            result = QueryIterNullIterator.create(execCxt);
        } else {

            Node outputNode = objects.get(0);

            Number outputValue = BindingUtils.getNumberNullable(binding, outputNode);
            if (outputValue != null) {
                // Check if the outputValue is within range
                long tmp = outputValue.longValue();
                boolean isInRange = tmp >= start && tmp < end;

                result = isInRange
                    ? QueryIterSingleton.create(binding, execCxt)
                    : QueryIterNullIterator.create(execCxt);

            } else {
                Var outputVar = (Var)outputNode;
                Iterator<Binding> it = LongStream.iterate(start, x -> x < end, x -> x + step)
                        .mapToObj(v -> BindingFactory.binding(binding, outputVar, NodeValue.makeInteger(v).asNode()))
                        .iterator();
                result = QueryIterPlainWrapper.create(it, execCxt);
            }
        }

        return result;
    }
}
