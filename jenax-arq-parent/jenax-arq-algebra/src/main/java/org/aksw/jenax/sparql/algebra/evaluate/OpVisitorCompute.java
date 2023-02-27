package org.aksw.jenax.sparql.algebra.evaluate;

import org.aksw.jenax.constraint.api.ConstraintRow;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.optimize.TransformScopeRename;
import org.apache.jena.sparql.algebra.walker.Walker;

public class OpVisitorCompute
   extends EvaluationBase<ConstraintRow>
{

    public static void main(String[] args) {
        Query query = QueryFactory.create("SELECT ?s { { ?s ?p ?o } { SELECT ?x { ?s ?p ?x } } } ORDER By ?p");
        Op op = Algebra.compile(query);
        // op = TransformScopeRename.transform(op) ;
        ConstraintRow tmp = Evaluator.evaluate(new OpVisitorCompute(), op);
        System.out.println(tmp);
        System.out.println(op);
    }

    @Override
    public ConstraintRow apply(OpBGP opBGP) {
        System.out.println("here");
        return null;
    }

}
