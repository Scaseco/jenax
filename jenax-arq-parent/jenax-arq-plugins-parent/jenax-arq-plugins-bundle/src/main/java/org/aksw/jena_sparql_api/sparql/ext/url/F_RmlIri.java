package org.aksw.jena_sparql_api.sparql.ext.url;

import java.util.List;

import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.norse.term.core.NorseTerms;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.ARQConstants;
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
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.function.FunctionBase1;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformCopyBase;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformer;

public class F_RmlIri extends FunctionBase1 {
    @Override
    protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
        String baseIRI = null;
        NodeValue nvRel = args.get(0);
        try {
            NodeValue result = resolve(nvRel, baseIRI, env);
            Node node = result.asNode();
            NodeUtils.validate(node);
            return result;
        } catch (ExprEvalException e) {
            throw new RuntimeException("Abort due to generation of invalid IRI: " + nvRel);
        }
    }

    @Override
    public NodeValue exec(NodeValue v) {
        throw new IllegalStateException("Should never be called");
    }

    /*package*/ static NodeValue resolve(NodeValue relative, String baseIRI, FunctionEnv env) {
        if ( baseIRI == null ) {
            if ( env.getContext() != null ) {
                Query query = (Query)env.getContext().get(ARQConstants.sysCurrentQuery);
                if ( query != null )
                    baseIRI = query.getBaseURI();
                // If still null, NodeFunctions.iri will use the system base.
//                if ( baseIRI == null )
//                    baseIRI = IRIs.getBaseStr();
            }
        }
        if ( NodeFunctions.isIRI(relative.asNode()) ) {
            relative = NodeValue.makeString(relative.asString());
        }

        return NodeFunctions.iri(relative, baseIRI);
    }

    public static class ExprTransformIriToRmlIri extends ExprTransformCopy {
        private static final ExprTransformIriToRmlIri INSTANCE = new ExprTransformIriToRmlIri();

        public static String IRI = NorseTerms.NS + "rml.iri";

        public static ExprTransform get() {
            return INSTANCE;
        }

        @Override
        public Expr transform(ExprFunction1 func, Expr expr1) {
            Expr result = func instanceof E_IRI ? new E_Function(IRI, new ExprList(expr1)) : super.transform(func, expr1);
            return result;
        }

        public static Expr transform(Expr expr) {
            return ExprTransformer.transform(ExprTransformIriToRmlIri.get(), expr);
        }

        public static Element transformElt(Element element) {
            Element result = ElementTransformer.transform(element, new ElementTransformCopyBase(true),
                    ExprTransformIriToRmlIri.get());
            return result;
        }
    }
}
