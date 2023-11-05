package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jenax.sparql.fragment.api.Fragment2;

public class OpForAll
    extends OpRoleRestriction
{
    public OpForAll(Fragment2 role, Op filler) {
        super(role, filler);
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
