package org.aksw.jenax.sparql.algebra.optimize;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.commons.path.core.Path;
import org.aksw.jenax.arq.util.expr.DnfUtils;
import org.aksw.jenax.arq.util.expr.ExprUtils;
import org.aksw.jenax.constraint.api.ConstraintRow;
import org.aksw.jenax.constraint.api.ValueSpace;
import org.aksw.jenax.constraint.impl.ConstraintRowMap;
import org.aksw.jenax.constraint.util.ConstraintDerivations;
import org.aksw.jenax.sparql.algebra.walker.Tracker;
import org.aksw.jenax.sparql.algebra.walker.TrackingTransformCopy;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.OpVisitorByTypeBase;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction1;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;


/**
 * This class checks for expressions of the form
 * [STR(?p) = 'const'] and if valueSpace(?p) is limited to IRI then it rewrites
 * the expression as [?p = &lt;const&gt;].
 *
 */
public class TrackingTransformConditionalFunctionInversion
    extends TrackingTransformCopy<ConstraintRow>
{
    // We could already derive constraints in a before visitor
    public class BeforeVisitor
        extends OpVisitorByTypeBase
    {
        @Override
        public void visit(OpBGP opBGP) {
//            System.out.println("Path before bgp: " + path());
        }

        @Override
        public void visit(OpFilter op) {
            // get conditions from parent + merge with the current ones
            // beforeConditions.put(path(), op.getExprs());
        }

        @Override
        public void visit(OpUnion opUnion) {
        }

    }


    protected BeforeVisitor beforeVisitor;


    protected Map<Path<String>, Map<Expr, ValueSpace>> beforeConditions = new LinkedHashMap<>();
    protected Map<Path<String>, Map<Expr, ValueSpace>> afterConditions = new LinkedHashMap<>();


    public TrackingTransformConditionalFunctionInversion(Tracker<ConstraintRow> pathState) {
        super(pathState);
        this.beforeVisitor = new BeforeVisitor();
    }

    @Override
    public OpVisitor getBeforeVisitor() {
        return beforeVisitor;
    }

//    @Override
//    public Op transform(OpProject opProject, Op subOp) {
//        Path<String> path = tracker.getPath();
////        System.out.println("Path at project: " + path);
//        return super.transform(opProject, subOp);
//    }

    @Override
    public Op transform(OpBGP opBGP) {
        ConstraintRow crow = tracker.computeIfAbsent(p -> ConstraintRowMap.create());

        for (Triple triple : opBGP.getPattern()) {
            ConstraintDerivations.deriveConstraints(crow, triple);
        }

        Path<String> path = tracker.getPath();
//        System.out.println("Path at bgp: " + path);

//        System.out.println("Constraints: " + crow);

        Map<Expr, ValueSpace> evsMap = toExprConstraints(crow);
        afterConditions.put(tracker.getPath(), evsMap);

        return super.transform(opBGP);
    }

    /**
     * Check for filter expressions of the form [str(?p) op 'string_literal'] and
     * if ?p is known to be an IRI transform it to [?p op <string_literal>]
     */
    @Override
    public Op transform(OpFilter opFilter, Op subOp) {

        // Check for expressions of the form (fn(?var) = const)
        ExprList el = opFilter.getExprs();
        List<List<Expr>> dnf = DnfUtils.toListDnf(el);


        Path<String> childPath = tracker.getChildPath(0);
        Map<Expr, ValueSpace> crow = afterConditions.get(childPath);

        if (crow != null) {

            // Set<Set<Expr>> newClauses
            for (List<Expr> clause : dnf) {

                for (int i = 0; i < clause.size(); ++i) {
                    Expr expr = clause.get(0);
                    Expr effExpr = expr;

                    // Find expressions of the form op(expr, const)
                    Entry<NodeValue, Expr> e = ExprUtils.applyToArgsOfBinaryExpr(expr, ExprUtils::tryGetConstAndExpr);

                    if (e != null) {
                        Entry<NodeValue, Expr> newE = applyInverseFunction(e, crow);

                        if (e != newE) {
                            effExpr = ExprUtils.copy(expr, newE.getValue(), newE.getKey());

                            clause.set(i, effExpr);
                        }
                    }
                }
            }
        }

        Expr finalExpr = DnfUtils.toExpr(dnf);
        Op result = OpFilter.filter(finalExpr, subOp);
        return result;
    }


    public static Map<Expr, ValueSpace> toExprConstraints(ConstraintRow crow) {
        Map<Expr, ValueSpace> result = new HashMap<>();
        for (Var var : crow.getVars()) {
            ValueSpace vs = crow.get(var);
            result.put(new ExprVar(var), vs);
        }
        return result;
    }

    public static Entry<NodeValue, Expr> applyInverseFunction(
            Entry<NodeValue, Expr> e,
            Map<Expr, ValueSpace> exprToValueSpace) {
            //ConstraintRow crow) {
        Entry<NodeValue, Expr> result = e;

        Expr expr = e.getValue();
        if (expr instanceof ExprFunction1) {
            ExprFunction1 fn = (ExprFunction1)expr;

            Expr arg = fn.getArg();

            if (fn instanceof E_Str) {
                ValueSpace vs = exprToValueSpace.get(arg);
                if (vs.isLimitedTo(org.apache.jena.sparql.expr.ValueSpace.VSPACE_URI)) {
                    result = new SimpleEntry<>(
                            org.apache.jena.sparql.util.ExprUtils.eval(new E_IRI(e.getKey())),
                            arg);
                }
            }
        }

        return result;
    }

//    @Override
//    public Op transform(OpUnion opUnion, Op left, Op right) {
//        if (path().getParent() != null) {
//            beforeConditions.put(path(), beforeConditions.get(path().getParent()));
//        }
//
//        System.out.println("Conditions at " + path() + ": " + beforeConditions.get(path()));
//        return super.transform(opUnion, left, right);
//    }



}
