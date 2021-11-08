package org.aksw.jenax.sparql.algebra.walker;

import java.util.function.Function;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVisitor;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.expr.ExprTransform;
import org.apache.jena.sparql.expr.ExprTransformCopy;

public class TrackingTransformer {
    public static <T> Op transform(Tracker<T> tracker, Function<? super Tracker<T>, ? extends TrackingTransformCopy<T>> transformCtor) {
        TrackingTransformCopy<?> transform = transformCtor.apply(tracker);
        OpVisitor beforeVisitor = transform.getBeforeVisitor();
        return transform(tracker, transform, new ExprTransformCopy(), beforeVisitor);
    }

    public static Op transform(Tracker<?> tracker, Transform opTransform, ExprTransform exprTransform, OpVisitor beforeVisitor) {
        TrackingApplyTransformVisitor applyTransformVisitor = new TrackingApplyTransformVisitor(tracker, opTransform, exprTransform, false, beforeVisitor, null);


        TrackingWalkerVisitor walker = new TrackingWalkerVisitor(tracker, applyTransformVisitor, applyTransformVisitor, beforeVisitor, null);
        Op inputOp = tracker.getPathToOp().get(tracker.getPath());
        walker.walk(inputOp);

        Op result = applyTransformVisitor.opResult();

        return result;
    }
}
