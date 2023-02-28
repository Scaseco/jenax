package org.aksw.jenax.sparql.algebra.evaluate;

import org.aksw.jenax.constraint.api.ConstraintRow;
import org.aksw.jenax.constraint.impl.ConstraintRowMap;
import org.aksw.jenax.constraint.util.ConstraintDerivations;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpOrder;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpQuadPattern;
import org.apache.jena.sparql.algebra.op.OpReduced;
import org.apache.jena.sparql.algebra.op.OpUnion;
import org.apache.jena.sparql.core.Quad;

public class EvaluationOfConstraints
   extends EvaluationBase<ConstraintRow>
{

    public static void main(String[] args) {
        Query query = QueryFactory.create("SELECT ?s { { ?s ?p ?o } { SELECT ?x { ?s ?p ?x } } } ORDER By ?p");
        Op op = Algebra.compile(query);
        System.out.println(op);
        // op = TransformScopeRename.transform(op) ;
        ConstraintRow tmp = Evaluator.evaluate(new EvaluationOfConstraints(), op);
        System.out.println(tmp);
    }

    @Override
    public ConstraintRow eval(OpBGP op) {
        ConstraintRow result = ConstraintRowMap.create();
        for (Triple triple : op.getPattern().getList()) {
            ConstraintDerivations.deriveConstraints(result, triple);
        }
        return result;
    }

    @Override
    public ConstraintRow eval(OpQuadPattern op) {
        ConstraintRow result = ConstraintRowMap.create();
        for (Quad quad : op.getPattern().getList()) {
            ConstraintDerivations.deriveConstraints(result, quad);
        }
        return result;
    }

    @Override
    public ConstraintRow eval(OpFilter op, ConstraintRow result) {
        // ConstraintDerivations.derive(result, null);
        return result;
    }

    @Override
    public ConstraintRow eval(OpJoin op, ConstraintRow lhs, ConstraintRow rhs) {
        lhs.stateIntersection(rhs);
        return lhs;
    }

    @Override
    public ConstraintRow eval(OpUnion op, ConstraintRow lhs, ConstraintRow rhs) {
        lhs.stateUnion(rhs);
        return lhs;
    }

    @Override
    public ConstraintRow eval(OpProject opProject, ConstraintRow result) {
        result.project(opProject.getVars());
        return result;
    }

    @Override
    public ConstraintRow eval(OpOrder opOrder, ConstraintRow result) {
        return result;
    }

    @Override
    public ConstraintRow eval(OpDistinct op, ConstraintRow result) {
        return result;
    }

    @Override
    public ConstraintRow eval(OpReduced op, ConstraintRow result) {
        return result;
    }

}
