package org.aksw.jena_sparql_api.sparql.ext.url;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;

public class E_IriAsGiven extends FunctionBase1 {
    public static final String tagIriAsGiven = "iriAsGiven";

    @Override
    public NodeValue exec(NodeValue nv) {
        NodeValue result;
        if (nv != null) {
            if (nv.isString()) {
                result = NodeValue.makeNode(NodeFactory.createURI(nv.getString()));
            } else if (nv.hasNode()) {
                if (nv.asNode().isURI()) {
                    result = nv;
                } else {
                    throw new ExprEvalException("E_IriAsGiven: Argument is not an IRI");
                }
            } else {
                throw new ExprEvalException("E_IriAsGiven: Argument is neither string nor node");
            }
        } else {
            throw new ExprEvalException("E_IriAsGiven: Called with null / unbound");
        }
        return result;
    }

    public static class ExprTransformIriToIriAsGiven extends ExprTransformCopy {
        private static final ExprTransformIriToIriAsGiven INSTANCE = new ExprTransformIriToIriAsGiven();

        public static String IRI = "http://jsa.aksw.org/fn/iri/asGiven";

        public static ExprTransform get() {
            return INSTANCE;
        }

        @Override
        public Expr transform(ExprFunction1 func, Expr expr1) {
            Expr result = func instanceof E_IRI ? new E_Function(IRI, new ExprList(expr1)) : super.transform(func, expr1);
            return result;
        }

        public static Expr transform(Expr expr) {
            return ExprTransformer.transform(ExprTransformIriToIriAsGiven.get(), expr);
        }

        public static Element transformElt(Element element) {
            Element result = ElementTransformer.transform(element, new ElementTransformCopyBase(true),
                    ExprTransformIriToIriAsGiven.get());
            return result;
        }
    }
}
