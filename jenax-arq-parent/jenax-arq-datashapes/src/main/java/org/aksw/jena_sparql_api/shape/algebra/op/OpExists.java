package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jenax.sparql.fragment.api.Fragment2;

public class OpExists
    extends OpRoleRestriction
{
    public OpExists(Fragment2 role, Op filler) {
        super(role, filler);
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
