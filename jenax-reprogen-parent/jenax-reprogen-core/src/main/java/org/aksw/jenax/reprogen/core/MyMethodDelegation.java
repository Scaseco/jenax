package org.aksw.jenax.reprogen.core;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.BiFunction;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.Super;
import net.bytebuddy.implementation.bind.annotation.SuperMethod;
import net.bytebuddy.implementation.bind.annotation.This;

// Not needed - subject for removal
//public class MyMethodDelegation {
//    protected Map<Method, BiFunction<Object, Object[], Object>> methodImplMap;
//
//    public MyMethodDelegation(Map<Method, BiFunction<Object, Object[], Object>> methodImplMap) {
//        super();
//        this.methodImplMap = methodImplMap;
//    }
//
//
//    @RuntimeType
//    public Object intercept(@Origin Method method, @This Object obj, @AllArguments Object[] args)
//            throws Exception {
//        return intercept(method, obj, null, null, args);
//    }
//
//    @RuntimeType
//    public Object intercept(@Origin Method method, @This Object obj, @Super Object s, @SuperMethod Method spr, @AllArguments Object[] args)
//            throws Exception {
//        BiFunction<Object, Object[], Object> delegate = methodImplMap.get(method);
////		    System.out.println(methodMap);
//        Object r;
//        if (delegate != null) {
//            r = delegate.apply(obj, args);
//        } else if (method.isDefault()) {
//            throw new RuntimeException("Should never come here anymore");
//        } else {
//            r = spr.invoke(s, args);
//        }
//
//        return r;
//    }
//
//}