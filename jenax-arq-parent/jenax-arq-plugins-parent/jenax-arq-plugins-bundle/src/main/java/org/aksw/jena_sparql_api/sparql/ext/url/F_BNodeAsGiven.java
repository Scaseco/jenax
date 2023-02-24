package org.aksw.jena_sparql_api.sparql.ext.url;

import org.aksw.jena_sparql_api.sparql.ext.init.SparqlX;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.sse.Tags;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;

public class F_BNodeAsGiven extends FunctionBase1 {
    public static final String tagBNodeAsGiven = "bnodeAsGiven";

    @Override
    public NodeValue exec(NodeValue nv) {
        NodeValue result;
        if (nv != null) {
            if (nv.isConstant()) {
                result = NodeValue.makeNode(NodeFactory.createBlankNode(nv.asUnquotedString()));
            } else {
                throw new ExprEvalException("E_BNodeAsGiven: Argument is not an constant");
            }
        } else {
            throw new ExprEvalException("E_BNodeAsGiven: Called with null / unbound");
        }
        return result;
    }

    public static class ExprTransformBNodeToBNodeAsGiven extends ExprTransformCopy {
        private static final ExprTransformBNodeToBNodeAsGiven INSTANCE = new ExprTransformBNodeToBNodeAsGiven();

        public static String IRI = SparqlX.NS + "bnode.asGiven";

        public static ExprTransform get() {
            return INSTANCE;
        }

        @Override
        public Expr transform(ExprFunction1 func, Expr expr1) {
            Expr result = Tags.tagBNode.equals(func.getFunctionSymbol().getSymbol())
                    ? new E_Function(IRI, new ExprList(expr1)) : super.transform(func, expr1);
            return result;
        }

        public static Expr transform(Expr expr) {
            return ExprTransformer.transform(ExprTransformBNodeToBNodeAsGiven.get(), expr);
        }

        public static Element transformElt(Element element) {
            Element result = ElementTransformer.transform(element, new ElementTransformCopyBase(true),
                    ExprTransformBNodeToBNodeAsGiven.get());
            return result;
        }
    }
}
