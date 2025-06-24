package org.aksw.jena_sparql_api.sparql.ext.sys;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.aksw.jenax.arq.datatype.lambda.Lambda;
import org.aksw.jenax.arq.datatype.lambda.Lambdas;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.expr.ExprEvalTypeException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Function that takes two mandatory and one optional parameters:
 * A lambda that generates a value
 * A lambda that must return true if the value is accepted
 * Optionally, a retry count.
 *
 * norse:fn_retry()
 */
public class FN_Retry
    extends FunctionBase
{
    private static final Logger logger = LoggerFactory.getLogger(FN_Retry.class);

    @Override
    protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        int n = args.size();

        // List<Expr> scopedExprs = args.getList();
        NodeValue supplierNv = args.get(0);
        NodeValue predicateNv = args.get(1);
        NodeValue retryCountNv = n > 2 ? args.get(2) : null;

        Context cxt = env.getContext();
        AtomicBoolean cancelSignal = cxt.get(ARQConstants.symCancelQuery);

        long retryCount = Long.MAX_VALUE;
        if (retryCountNv != null) {
            if (retryCountNv.isInteger()) {
                retryCount = retryCountNv.getInteger().longValue();
            } else {
                NodeValue.raise(new ExprEvalTypeException("Expected a long value for the retry count (third argument)"));
            }
        }

        Lambda supplier = Lambdas.extract(supplierNv);
        Lambda predicate = Lambdas.extract(predicateNv);

        if (!supplier.getParams().isEmpty()) {
            // NodeValue.raise(new ExprEvalTypeException("Lambda"));
            throw new RuntimeException("Lambda for supplying values must not accept any arguments");
        }

        if (predicate.getParams().size() != 1) {
            // NodeValue.raise(new ExprEvalTypeException("Lambda"));
            throw new RuntimeException("Lambda for validation values must only accept one argument.");
        }

        NodeValue result = null;
        long i;
        // Repeat as long as i is less than the retry count and the cancel signal is either null or false
        for (i = 0; i < retryCount && (cancelSignal == null || !cancelSignal.get()); ++i) {
            NodeValue tmp = Lambdas.eval(supplier, List.of(), env);

            NodeValue verdict = null;
            try {
                verdict = Lambdas.eval(predicate, List.of(tmp), env);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.info("Lambda evaluation raised an exception.", e);
                }
            }

            if (logger.isInfoEnabled()) {
                logger.info("Verdict for {} is {}. Value supplied by {} and evaluated with {}.", tmp, verdict, supplier, predicate);
            }

            if (verdict != null && Boolean.TRUE.equals(verdict.getBoolean())) {
                result = tmp;
                break;
            }
        }

        if (result == null) {
            NodeValue.raise(new ExprEvalTypeException("Failed to compute a non-null result after " + i + " retries"));
        }

        return result;
    }

    @Override
    public NodeValue exec(List<NodeValue> args) {
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    public void checkBuild(String uri, ExprList args) {
        int n = args.size();
        if (!(n >= 2 && n <= 3)) {
            throw new QueryBuildException("Function '" + Lib.className(this) + "' takes two or three arguments");
        }
    }
}
