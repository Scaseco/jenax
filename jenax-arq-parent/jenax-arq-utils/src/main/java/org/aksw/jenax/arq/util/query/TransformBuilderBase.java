package org.aksw.jenax.arq.util.query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.aksw.jenax.arq.util.op.OpTransform;

//public abstract class TransformBuilderBase<X, Y, T extends Function<X, X>, U extends Function<Y, Y>> {
//    protected List<T> queryTransforms = new ArrayList<>();
//    protected List<U> pending = new ArrayList<>();
//
//    protected abstract T newSuperTransform(List<T> members);
//    protected abstract U newSubTransform(List<U> members);
//    protected abstract T subToSuper(U sub);
//
//    protected void closePending() {
//        U next = TransformList.flattenOrNull(true, this::newSubTransform, pending);
//        if (next != null) {
//            T s = subToSuper(next);
//            queryTransforms.add(s);
//            pending.clear();
//        }
//    }
//
//    public QueryTransformBuilder add(QueryTransform transform) {
//        // Unnest rewrite transform
//        if (transform instanceof QueryTransformViaRewrite t) {
//            OpTransform rewrite = t.getOpTransform();
//            TransformList.streamFlatten(true, rewrite).forEach(pending::add);
//        } else {
//            closePending();
//            queryTransforms.add(transform);
//        }
//        return this;
//    }
//
//    public QueryTransformBuilder add(OpTransform transform) {
//        pending.add(transform);
//        return this;
//    }
//
//    public T build() {
//        return TransformList.flattenOrNull(true, this::newSuperTransform, queryTransforms);
//    }
//}
