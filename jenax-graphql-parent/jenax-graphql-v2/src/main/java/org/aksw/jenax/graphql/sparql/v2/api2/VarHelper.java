package org.aksw.jenax.graphql.sparql.v2.api2;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.PatternVars;

public class VarHelper {
    public static Set<Var> visibleVars(Op op) {
        Set<Var> result;
        if (op instanceof OpExtend) {
            // TODO OpVars.visibleVars does not report variables on the lhs as visible, although we may use them for lateral joins.
            //   Expected behavior or bug?
            // TODO OpVars.mentionedVars does not report the output variable: OpVars(BIND(?x AS ?y)) -> { ?x } same for PatternVars.vars(element)
            result = new LinkedHashSet<>(OpVars.visibleVars(op));
            result.addAll(OpVars.mentionedVars(op));
            // visibleVars = new LinkedHashSet<>(PatternVars.vars(element)); // new LinkedHashSet<>(OpVars.mentionedVars(op));
        } else if (op instanceof OpFilter f) {
            result = f.getExprs().getVarsMentioned();
        } else {
            result = OpVars.visibleVars(op);
        }
        return result;
    }

    public static Set<Var> vars(Element elt) {
        Set<Var> result;
        if (elt instanceof ElementBind b) {
            result = visibleVars(Algebra.compile(b));
        } else if (elt instanceof ElementFilter f) {
            result = f.getExpr().getVarsMentioned();
        } else {
            result = new LinkedHashSet<>(PatternVars.vars(elt));
        }
        return result;
    }
}
