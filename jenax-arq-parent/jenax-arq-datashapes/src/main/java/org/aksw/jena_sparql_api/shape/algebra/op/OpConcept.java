package org.aksw.jena_sparql_api.shape.algebra.op;

import org.aksw.jenax.sparql.fragment.impl.Concept;

public class OpConcept
    extends Op0
{
    protected Concept concept;

    public OpConcept(Concept concept) {
        super();
        this.concept = concept;
    }

    public Concept getConcept() {
        return concept;
    }

    @Override
    public <T> T accept(OpVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

}
