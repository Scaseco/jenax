package org.aksw.jena_sparql_api.sparql.ext.binding;

import org.aksw.jenax.arq.util.var.VarUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprEvalTypeException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.sparql.function.FunctionBase2;

/**
 * Access a JSON objects immediate attribute.
 * Experiment for whether this is significantly faster than evaluating json path expressions.
 *
 * jsonLiteral json:get(jsonLiteral, key)
 *
 * @author raven
 *
 */
public class F_BindingGet
    extends FunctionBase2
{
    public F_BindingGet() {
        super();
    }

    @Override
    public NodeValue exec(NodeValue nv, NodeValue key) {
        Binding elt = JenaBindingUtils.requireBinding(nv);
        NodeValue result = null;
        if (key.isString()) {
            String str = key.getString();
            // TODO Not ideal to rely on safeVarName here
            String safeStr = VarUtils.safeVarName(str);
            Node node = elt.get(safeStr);
            if (node == null) {
                NodeValue.raise(new VariableNotBoundException());
            }
            result = NodeValue.makeNode(node);
        } else {
            NodeValue.raise(new ExprEvalTypeException("Json array or object expected"));
        }

        if (result == null) {
            NodeValue.raise(new ExprEvalException("Access of JSON object by key returned null value"));
        }

        return result;
    }
}
