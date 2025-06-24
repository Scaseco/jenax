package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.model.udf.util.UserDefinedFunctions;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionN;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.function.user.ExprTransformExpand;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;


/** Wrapper for {@link ExprTransformExpand} which repeatedly expands
 *  until there is no more change. Also applies constant folding after expansion. */
public class ExprTransformPrettyMacroExpansion
    extends ExprTransformCopy
{
    protected Map<String, UserDefinedFunctionDefinition> udfRegistry;

    public ExprTransformPrettyMacroExpansion(Map<String, UserDefinedFunctionDefinition> udfRegistry) {
        super();
        this.udfRegistry = Objects.requireNonNull(udfRegistry);
    }

    @Override
    public Expr transform(ExprFunctionN func, ExprList args) {
        // XXX Could avoid func.copy()
        Expr result = UserDefinedFunctions.expandMacro(udfRegistry, func.copy(args));
        return result;
    }
}
