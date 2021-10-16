package org.aksw.jenax.reprogen.descriptor.impl;

import org.aksw.jenax.reprogen.core.MethodDescriptor;
import org.aksw.jenax.reprogen.hashid.MethodGroup;

public class MethodGroupImpl
    implements MethodGroup
{
    protected Class<?> clazz;
    protected String methodName;
    protected SimpleType beanType;

    protected MethodDescriptor getter;
    protected MethodDescriptor setter;
    protected MethodDescriptor dynamicGetter;


    public MethodGroupImpl(Class<?> clazz, String methodName, SimpleType beanType) {
        super();
        this.clazz = clazz;
        this.methodName = methodName;
        this.beanType = beanType;
    }

    @Override
    public String getName() {
        return methodName;
    }

    @Override
    public MethodDescriptor getter() {
        return getter;
    }

    @Override
    public MethodDescriptor setter() {
        return setter;
    }


    @Override
    public MethodDescriptor dynamicGetter() {
        return dynamicGetter;
    }

    public void add(MethodDescriptor m) {
        // Validate that the method belongs to this group
        // clazz.isAssignableFrom().(m.getMethod().getDeclaringClass())
        //m.getMethod().getName().equals(name)

        if(m.isGetter()) {
            getter = m;
        } else if(m.isSetter()) {
            setter = m;
        }
    }

    @Override
    public SimpleType getBeanType() {
        return beanType;
    }
}
