package org.aksw.jena_sparql_api.algebra.expr.transform;

import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.util.function.FixpointIteration;
import org.aksw.jena_sparql_api.algebra.transform.TransformExprToBasicPattern;
import org.aksw.jena_sparql_api.algebra.transform.TransformPullFiltersIfCanMergeBGPs;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import com.google.common.collect.Maps;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.optimize.TransformMergeBGPs;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class ExprTransformMacroExpansion
    // extends ExprTransformCopy
{
    protected Map<String, UserDefinedFunctionDefinition> macros;
    protected Map<String, Boolean> propertyFunctions;

    public ExprTransformMacroExpansion(
            Map<String, UserDefinedFunctionDefinition> macros,
            Map<String, Boolean> propertyFunctions) {
        super();
        this.macros = macros;
        this.propertyFunctions = propertyFunctions;
    }

    private static final Logger logger = LoggerFactory.getLogger(ExprTransformMacroExpansion.class);

    public Op rewrite(Op op) {
        Op c = op;

        Op d = TransformExprToBasicPattern.transform(c, fn -> {
            String id = org.aksw.jenax.arq.util.expr.ExprUtils.getFunctionId(fn.getFunction());
            Boolean subjectAsOutput = propertyFunctions.get(id);
            Entry<String, Boolean> r = subjectAsOutput == null ? null : Maps.immutableEntry(id, subjectAsOutput);
//            //System.out.println(id);
//            if("str".equals(id)) {
//                return Maps.immutableEntry("http://foo.bar/baz", false);
//            }
            return r;
        });

        Op e = FixpointIteration.apply(d, x -> {
            x = TransformPullFiltersIfCanMergeBGPs.transform(x);
            x = Transformer.transform(new TransformMergeBGPs(), x);
            return x;
        });

        return e;
    }

    public Query rewrite(Query query) {
        Query result = QueryUtils.rewrite(query, this::rewrite);
        logger.debug("Rewrote query\n" + query + " to\n" + result);
        return result;
    }
}

