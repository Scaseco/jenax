package org.aksw.jenax.sparql.algebra.eval;

import java.util.Map.Entry;

import org.aksw.jenax.constraint.api.CBinding;
import org.aksw.jenax.constraint.api.VSpace;
import org.aksw.jenax.constraint.impl.CBindingMap;
import org.aksw.jenax.constraint.util.ConstraintDerivations;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.algebra.op.OpGroup;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.op.OpSlice;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

public class EvaluationOfConstraints
   extends EvaluatorBase<CBinding>
{
    public EvaluationOfConstraints() {
        this.dispatcher = new EvaluationDispatch<>(this);
    }

    public static void main(String[] args) {
        Query query = QueryFactory.create("SELECT ?x { { ?s ?p ?o } UNION {"
                + "SELECT ?x { ?s ?p ?o BIND(IRI(CONCAT('foo', ?bar)) AS ?x) } } } ORDER By ?p");
        Op op = Algebra.compile(query);
        System.out.println(op);
        // op = TransformScopeRename.transform(op) ;
        Evaluator<CBinding> evaluator = new EvaluationOfConstraints();

        CBinding tmp = evaluator.evalOp(op, CBindingMap.create());
        System.out.println(tmp);
    }

    @Override
    public CBinding eval(OpBGP op, CBinding input) {
        CBinding result = CBindingMap.create();
        for (Triple triple : op.getPattern().getList()) {
            ConstraintDerivations.deriveConstraints(result, triple);
        }
        return result;
    }

    @Override
    public CBinding eval(OpQuadPattern op, CBinding input) {
        CBinding result = CBindingMap.create();
        for (Quad quad : op.getPattern().getList()) {
            ConstraintDerivations.deriveConstraints(result, quad);
        }
        return result;
    }

    @Override
    public CBinding eval(OpFilter op, CBinding input) {
        CBinding result = evalOp(op.getSubOp(), input);
        // result.project(op.getVars());
        // ConstraintDerivations.derive(result, null);
        return result;
    }

    @Override
    public CBinding eval(OpJoin op, CBinding input) {
        CBinding left = eval(op.getLeft(), input);
        CBinding right = eval(op.getRight(), left);

        CBinding result = left.cloneObject();
        result.stateIntersection(right);
        return result;
    }

    @Override
    public CBinding eval(OpUnion op, CBinding input) {
        CBinding left = eval(op.getLeft(), input);
        CBinding right = eval(op.getRight(), input);
        CBinding result = left.cloneObject().stateUnion(right);
        return result;
    }

    @Override
    public CBinding eval(OpSlice op, CBinding input) {
        CBinding result = eval(op.getSubOp(), input);
        return result;
    }

    @Override
    public CBinding eval(OpProject op, CBinding input) {
        CBinding base = eval(op.getSubOp(), input);
        CBinding result = base.cloneObject().project(op.getVars());
        return result;
    }

    @Override
    public CBinding eval(OpOrder op, CBinding input) {
        CBinding result = eval(op.getSubOp(), input);
        return result;
    }

    @Override
    public CBinding eval(OpDistinct op, CBinding input) {
        CBinding result = eval(op.getSubOp(), input);
        return result;
    }

    @Override
    public CBinding eval(OpReduced op, CBinding input) {
        CBinding result = eval(op.getSubOp(), input);
        return result;
    }

    // FIXME There should be a predicate whether the SERVICE clause can be analyzed as a standard
    //   graph pattern
    @Override
    public CBinding eval(OpService opService, CBinding input) {
        return CBindingMap.create();
    }

    @Override
    public CBinding eval(OpGroup op, CBinding input) {
        CBinding result = eval(op.getSubOp(), input);
        return result;
    }

    @Override
    public CBinding eval(OpGraph op, CBinding input) {
        CBinding result = eval(op.getSubOp(), input);
        return result;
    }

    @Override
    public CBinding eval(OpExtend op, CBinding input) {
        CBinding tmp = eval(op.getSubOp(), input);
        if (tmp == null) {
            System.err.println("WARN: sub op evaluated to null");
        }
        CBinding result = tmp.cloneObject();
        for (Entry<Var, Expr> entry : op.getVarExprList().getExprs().entrySet()) {
            Var v = entry.getKey();
            Expr e = entry.getValue();
            VSpace vc = ConstraintDerivations.deriveValueSpace(e, result);
            result.stateIntersection(v, vc);
        }
        return result;
    }
}
