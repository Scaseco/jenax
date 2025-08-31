package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.Objects;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.algebra.optimize.ExprTransformConstantFold;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.E_URI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExprTransformConstantFoldWithIris extends ExprTransformConstantFold {

    private static final Logger logger = LoggerFactory.getLogger(ExprTransformConstantFoldWithIris.class);

    private FunctionEnv functionEnv;
    private String functionEnvBaseUri = null;

    public ExprTransformConstantFoldWithIris() {
        this(ExecutionContext.create(ARQ.getContext().copy()));
    }

    public ExprTransformConstantFoldWithIris(FunctionEnv functionEnv) {
        super();
        this.functionEnv = Objects.requireNonNull(functionEnv);
        this.functionEnvBaseUri = extractBaseUri(functionEnv.getContext());
    }

    public FunctionEnv getFunctionEnv() {
        return functionEnv;
    }

    public String getFunctionEnvBaseUri() {
        return functionEnvBaseUri;
    }

    private static String extractBaseUri(Context context) {
        Query query = null;
        if (context != null) {
            try {
                query = (Query)context.get(ARQConstants.sysCurrentQuery);
            } catch (Exception e) {
                logger.warn("Unexpected error", e);
            }
        }

        String result = query == null
            ? null
            : query.getBaseURI();

        return result;
    }

    @Override
    public Expr transform(ExprFunction1 func, Expr expr1) {
        Expr r = null;
        if (expr1.isConstant()) {
            NodeValue nv = expr1.getConstant();
            try {
                if (func instanceof E_IRI e_iri) {
                    if (isEligibleForEval(e_iri.getParserBase())) {
                        r = e_iri.eval(nv, getFunctionEnv());
                    }
                } else if (func instanceof E_URI e_uri) {
                    if (isEligibleForEval(e_uri.getParserBase())) {
                        r = e_uri.eval(nv, getFunctionEnv());
                    }
                }
            } catch (ExprEvalException x) {
                // Do not eval.
            }
        }

        if (r == null) {
            r = super.transform(func, expr1);
        }
        return r;
    }

//    @Override
//    public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {
//        Expr r = null;
//        if ((expr1 != null || expr1.isConstant()) && expr2.isConstant()) {
//            NodeValue baseNv = expr1 == null ? null : expr1.getConstant();
//            NodeValue relNv = expr2.getConstant();
//
//            try {
//                if (func instanceof E_IRI2 e_iri) {
//                    if (isEligibleForEval(e_iri.getParserBase())) {
//                        r = e_iri.eval(baseNv, relNv, getFunctionEnv());
//                    }
//                } else if (func instanceof E_URI2 e_uri) {
//                    if (isEligibleForEval(e_uri.getParserBase())) {
//                        r = e_uri.eval(baseNv, relNv, getFunctionEnv());
//                    }
//                }
//            } catch (ExprEvalException x) {
//                // Do not eval.
//            }
//        }
//
//        if (r == null) {
//            r = super.transform(func, expr1, expr2);
//        }
//        return r;
//    }

    /** Only eval if there is an explicit base IRI. */
    protected boolean isEligibleForEval(String base) {
        boolean result = functionEnvBaseUri != null || base != null;
        return result;
    }
}
