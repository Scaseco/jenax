package org.aksw.jenax.sparql.algebra.evaluate;

import java.util.Objects;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.algebra.walker.Walker;

public class Evaluator {

    private static Evaluator singleton = new Evaluator();

    /** Get the current transformer */
    public static Evaluator get() { return singleton; }

    /** Set the current transformer - use with care */
    public static void set(Evaluator value) { Evaluator.singleton = value; }

    /** Evaluate an algebra expression */
    public static <T> T evaluate(Evaluation<T> evaluation, Op op) {
        return evaluate(evaluation, op, null, null);
  }

    /** Evaluate an algebra expression */
    public static <T> T evaluate(Evaluation<T> evaluation, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        return get().evaluate$(evaluation, op, beforeVisitor, afterVisitor) ;
    }

    public <T> T evaluate$(Evaluation<T> transform, Op op) {
        return evaluate$(transform, op, null, null) ;
    }

    public <T> T evaluate$(Evaluation<T> v, Op op, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        ApplyEvaluationVisitor<T> evaluationVisitor = createEvaluator(v);
        return evaluate(op, evaluationVisitor, beforeVisitor, afterVisitor);
    }

    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static <T> T evaluateSkipService(Evaluation<T> evaluation, Op op) {
        return evaluateSkipService(evaluation, op, null, null) ;
    }

    /** Transform an algebra expression except skip (leave alone) any OpService nodes */
    public static <T> T evaluateSkipService(Evaluation<T> evaluation, Op op,
                                          OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        if ( evaluation == null )
            evaluation = new EvaluationBase<>();
        //Evaluation<T> transform2 = new EvaluationSkipService<>(evaluation) ;
        // transform2 = opTransform ;
        ApplyEvaluationVisitor<T> aev = new ApplyEvaluationVisitor<>(evaluation) ;
        Walker.walkSkipService(op, aev, null, beforeVisitor, afterVisitor);
        T result = aev.opResult();
        return result;
    }

    public <T> ApplyEvaluationVisitor<T> createEvaluator(Evaluation<T> evaluation) {
        Objects.requireNonNull(evaluation);
        return new ApplyEvaluationVisitor<>(evaluation);
    }

    /** Evaluate an {@link Op}. */
    public <T> T evaluate(Op op, ApplyEvaluationVisitor<T> v, OpVisitor beforeVisitor, OpVisitor afterVisitor) {
        Walker.walk(op, v, null, beforeVisitor, afterVisitor);
        T result = v.opResult();
        return result;
    }

    // --------------------------------
    // Safe: ignore evaluation of OpService and return null.
    // Still walks the sub-op of OpService unless combined with a walker that does not go
    // down SERVICE
    static class EvaluationSkipService<T> extends EvaluationWrapper<T> {
        public EvaluationSkipService(Evaluation<T> evaluator) {
            super(evaluator) ;
        }

        @Override
        public T apply(OpService opService, T arg) {
            return null;
        }
    }
}
