package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jenax.sparql.fragment.api.Fragment2;

public abstract class OpRoleRestriction
    extends Op1 // TODO use the appropriate one
{
    protected Fragment2 role;

    public OpRoleRestriction(Fragment2 role, Op filler) {
        super(filler);
        this.role = role;
        //this.filler = filler;
    }

    public Op getFiller()
    {
        return subOp;
    }

    public Fragment2 getRole() {
        return role;
    }
}
