package org.aksw.facete.v3.api.path;

import org.aksw.jenax.sparql.relation.api.BinaryRelation;

public class StepSpecFromRelement2Impl
    implements StepSpecFromRelement2
{
    protected BinaryRelation br;

    public StepSpecFromRelement2Impl(BinaryRelation br) {
        super();
        this.br = br;
    }

    @Override
    public BinaryRelation getRelement() {
        return br;
    }

}
