package org.aksw.jena_sparql_api.algebra.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.generator.Generator;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.arq.util.var.VarGeneratorImpl2;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.TransformCopy;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.algebra.op.OpDistinct;
import org.apache.jena.sparql.algebra.op.OpExtend;
import org.apache.jena.sparql.algebra.op.OpFilter;
import org.apache.jena.sparql.algebra.op.OpLeftJoin;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_Exists;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunctionOp;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformCopy;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Transformer that rewrites
 * <pre>
 * FILTER EXISTS { pattern }
 * </pre>
 * into
 * <pre>
 * OPTIONAL { pattern }
 * FILTER BOUND(?v) # where ?v is an arbitrary variable exclusive to pattern
 * </pre>
 *
 * This is generally NOT an equivalence transformation, as the optional block may cause altering the
 * cardinalities of bindings from the left hand side.
 */
public class TransformExistsToOptional
    extends TransformCopy
{
    protected Generator<Var> varGen;

    public static TransformExistsToOptional create(Op inputOp) {
        Generator<Var> varGen = VarGeneratorBlacklist.create("__exists", OpVars.mentionedVars(inputOp));
        return new TransformExistsToOptional(varGen);
    }

    public TransformExistsToOptional() {
        this(VarGeneratorImpl2.create("__exists"));
    }

    public TransformExistsToOptional(Generator<Var> varGen) {
        super();
        this.varGen = varGen;
    }

    @Override
    public Op transform(OpFilter opFilter, Op subOp) {
        ExprTransformExistsToOptional exprTransform = new ExprTransformExistsToOptional(subOp);
        ExprList oldExprs = opFilter.getExprs();
        ExprList newExprs = ExprTransformer.transform(exprTransform, oldExprs);

        Op result;
        if (!oldExprs.equals(newExprs)) {
            Op lhs = exprTransform.getCurrentLhs();
            result = OpFilter.filterBy(newExprs, lhs);
            result = new OpProject(result, new ArrayList<>(exprTransform.getInitialLhsVisibleVars()));
        } else {
            result = super.transform(opFilter, subOp);
        }

        return result;

    }

    public class ExprTransformExistsToOptional
        extends ExprTransformCopy
    {
        protected Op lhs;

        // The variables of the initial lhs
        // The field lhs is updated with a OpLeftJoin whenever a (not) exists expression is encountered
        protected Set<Var> initialLhsVisibleVars;

        public Op getCurrentLhs() {
            return lhs;
        }

        public ExprTransformExistsToOptional(Op lhs) {
            this(lhs, OpVars.visibleVars(lhs));
        }

        public ExprTransformExistsToOptional(Op lhs, Set<Var> lhsVisibleVars) {
            super();
            this.lhs = lhs;
            this.initialLhsVisibleVars = lhsVisibleVars;
        }

        public Set<Var> getInitialLhsVisibleVars() {
            return initialLhsVisibleVars;
        }

        @Override
        public Expr transform(ExprFunctionOp funcOp, ExprList args, Op rhs) {
            Expr result = null;

            boolean isNotExists = funcOp instanceof E_NotExists;
            boolean isExists = funcOp instanceof E_Exists;

            if (isExists || isNotExists) {
                // TODO Probably we need to rename out-of scope variables in rhs to avoid clashes across multiple EXISTS expressions
                Set<Var> joinVarSet = OpVars.visibleVars(rhs);
                joinVarSet.retainAll(initialLhsVisibleVars);

                List<Var> joinVars = new ArrayList<>(joinVarSet);

                Var existsVar = varGen.next();
                joinVars.add(existsVar);

                Op newRhs = rhs;

                newRhs = OpExtend.extend(newRhs, existsVar, NodeValue.TRUE);

                if (!joinVars.isEmpty()) {
                    newRhs = new OpProject(newRhs, joinVars);
                }

                newRhs = OpDistinct.create(newRhs);

                Op newOp = OpLeftJoin.create(lhs, newRhs, (Expr)null);
                // newOp = OpFilter.filter(new E_Equals(new ExprVar(existsVar), NodeValue.TRUE), newOp);

                lhs = newOp;

                result = new E_Equals(new ExprVar(existsVar), NodeValue.TRUE);

                if (isNotExists) {
                    result = new E_LogicalNot(result);
                }
            } else {
                result = super.transform(funcOp, args, rhs);
            }
            return result;
        }
    }

    public static void main(String[] args) {
        String str = """
        SELECT * {
          ?s ?p ?o
          FILTER EXISTS { ?s <urn:label> ?l }
          FILTER NOT EXISTS { ?s <urn:knows> ?l }
        }
        """;

        Op beforeOp = Algebra.compile(QueryFactory.create(str));
        Op afterOp = Transformer.transform(TransformExistsToOptional.create(beforeOp), beforeOp);
        Query afterQuery = OpAsQuery.asQuery(afterOp);
        System.out.println(afterQuery);
    }
}
