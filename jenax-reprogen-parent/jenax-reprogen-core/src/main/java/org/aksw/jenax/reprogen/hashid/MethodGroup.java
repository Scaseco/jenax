package org.aksw.jenax.reprogen.hashid;

import org.aksw.jenax.reprogen.core.MethodDescriptor;
import org.aksw.jenax.reprogen.descriptor.impl.SimpleType;

/**
 * A set of methods with the same name, with certain signatures corresponding to
 * specific purposes, namely
 * getter, setter, dynamic setter
 *
 * @author raven
 *
 */
public interface MethodGroup {
    String getName();
    SimpleType getBeanType();

    MethodDescriptor getter();
    MethodDescriptor setter();
    MethodDescriptor dynamicGetter();

//    MethodDescriptor collectionAccessor();
    // E.g: .getDcatDistributions(MyDcatDist.class);

//    MethodDescriptor collectionSetter();
//    // setFoo(List<? extends T>)
//    MethodDescriptor setSetter();
//    MethodDescriptor listSetter();
}
