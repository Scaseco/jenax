package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jenax.sparql.relation.api.BinaryRelation;

public class OpForAll
    extends OpRoleRestriction
{
    public OpForAll(BinaryRelation role, Op filler) {
        super(role, filler);
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }
}
