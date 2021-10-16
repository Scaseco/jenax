package org.aksw.jenax.reprogen.descriptor.impl;

public abstract class TypeBase
    implements SimpleType
{
    @Override
    public boolean isScalar() {
        return false;
    }

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public TypeScalar asScalar() {
        return isScalar() ? (TypeScalar)this : null;
    }

    @Override
    public TypeCollection asCollection() {
        return isCollection() ? (TypeCollection)this : null;
    }
}
