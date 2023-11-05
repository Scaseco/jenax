package org.aksw.facete.v3.api.path;

import org.aksw.jenax.sparql.fragment.api.Fragment2;

public class StepSpecFromRelement2Impl
    implements StepSpecFromRelement2
{
    protected Fragment2 br;

    public StepSpecFromRelement2Impl(Fragment2 br) {
        super();
        this.br = br;
    }

    @Override
    public Fragment2 getRelement() {
        return br;
    }

}
