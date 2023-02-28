package org.aksw.jenax.constraint.util;

import java.util.List;

import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.aksw.jenax.arq.util.quad.QuadUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.constraint.api.CBinding;
import org.aksw.jenax.constraint.api.RdfTermProfiles;
import org.aksw.jenax.constraint.api.VSpace;
import org.aksw.jenax.constraint.impl.VSpaceImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.core.mem.TupleSlot;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.Range;

public class ConstraintDerivations {

    /** Derive constraints from a quad; merges them into the RowConstraints instance */
    public static void deriveConstraints(CBinding row, Quad quad) {

        for (int i = 0; i < 3; ++i) {
            Node node = QuadUtils.getNode(quad, i);

            // For variables we can derive the term type from the slot
            if (node.isVariable()) {
                Var var = (Var)node;
                // Get the appropriate constraints for the slot
                TupleSlot slot = QuadUtils.idxToSlot(i);

                VSpace vs = RdfTermProfiles.forSlot(slot);
                row.stateIntersection(var, vs);
            }
        }
    }

    public static void deriveConstraints(CBinding row, Triple triple) {

        for (int i = 0; i < 3; ++i) {
            Node node = TripleUtils.getNode(triple, i);

            // For variables we can derive the term type from the slot
            if (node.isVariable()) {
                Var var = (Var)node;
                // Get the appropriate constraints for the slot
                TupleSlot slot = TripleUtils.idxToSlot(i);

                VSpace vs = RdfTermProfiles.forSlot(slot);
                row.stateIntersection(var, vs);
            }
        }
    }

    /** ElementBind */
    public static void deriveConstraints(CBinding row, VarExprList defs) {

    }

    public static void derive(CBinding row, Expr expr) {
        // TODO We need expr utils to extract op(var, constant) expressions - but they are in a higher module...
        // One possible solution is to define an interface at the lower level and provide the implementation in a higher one

        VSpace c = row.get(null);

        // TODO Recognize term type functions:
        // IRI(x), BNODE(y), STRDT(?, constantDatatype),
        //
        // TODO Recognize IRI(concat("const"))

    }


    /** Attempt to figure out what the expression might return */
    public static VSpace deriveValueSpace(Expr expr, CBinding cxt) {
        VSpace result = null;
        if (expr.isConstant()) {
            NodeValue nv = expr.getConstant();
            result = VSpaceImpl.create(NodeRanges.createClosed().addValue(nv.asNode()));
        } else if (expr.isVariable()) {
            Var var = expr.asVar();
            result = cxt.get(var);
        } else {
            List<Expr> args = ExprUtils.getSubExprs(expr);
            if (expr instanceof E_StrConcat) {
                // Folding consecutive string args into one is a separate task that should be run first
                if (args.isEmpty()) {
                    // Empty string
                    result = VSpaceImpl.create(NodeRanges.createClosed().addValue(NodeFactory.createLiteral("")));
                } else {
                    // Only look at the first arg
                      Expr arg = args.get(0);
                    // Argument might be STR("foo") - so we have to recurse
//                    if (arg.isConstant()) {
//                        NodeValue nv = arg.getConstant();
//                        if (nv.isString()) {
//                            String str = nv.asString();
//                            result = ValueSpaceImpl.create(NodeRanges.nodeRangeForPrefix(str));
//                        }
//                    } else {
//                    	result =
//                    }
//                    if (result == null) {
//                        result = ValueSpaceImpl.create(NodeRanges.createOpen());
//                    }
                    result = deriveValueSpace(arg, cxt);
                    // if arg is a string then create a prefix constraint from it
                    // result = deriveValueSpace(arg, cxt);

                    // If the result has values in the string space then use those as prefixes

                    result.stateIntersection(VSpaceImpl.create(NodeRanges.createClosed()
                            .addOpenDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_UNDEF)
                            .addOpenDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_STRING)));
                }
            } else if (expr instanceof E_Str) {
                Expr arg = args.get(0);
                result = deriveValueSpace(arg, cxt);
                result.stateIntersection(VSpaceImpl.create(NodeRanges.createClosed()
                        .addOpenDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_UNDEF)
                        .addOpenDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_STRING)));
            } else if (expr instanceof E_IRI) {
                // TODO Consider the BASE IRI / relative IRIs
                Expr arg = args.get(0);
                VSpace argSpace = deriveValueSpace(arg, cxt);
                result = argSpace.forDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_STRING);
                result.moveDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_STRING, org.apache.jena.sparql.expr.ValueSpace.VSPACE_URI);
                if (!argSpace.isLimitedTo(org.apache.jena.sparql.expr.ValueSpace.VSPACE_STRING)) {
                    result.stateUnion(VSpaceImpl.create(NodeRanges.createClosed()
                        .addOpenDimension(org.apache.jena.sparql.expr.ValueSpace.VSPACE_UNDEF)));
                }
            }
        }

        if (result == null) {
            result = VSpaceImpl.create(NodeRanges.createOpen());
        }
        return result;
    }


}
