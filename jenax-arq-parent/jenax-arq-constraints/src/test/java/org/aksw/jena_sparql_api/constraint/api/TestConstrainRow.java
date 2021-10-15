package org.aksw.jena_sparql_api.constraint.api;

import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.constraint.api.ConstraintRow;
import org.aksw.jenax.constraint.impl.ConstraintRowMap;
import org.aksw.jenax.constraint.util.ConstraintUtils;
import org.apache.jena.sparql.core.Quad;
import org.junit.Test;

public class TestConstrainRow {

    @Test
    public void test() {
        ConstraintRow cr = ConstraintRowMap.create();

        Quad quad = Quad.create(Vars.g, Vars.s, Vars.p, Vars.o);

        ConstraintUtils.deriveConstraints(cr, quad);

        System.out.println(cr);
    }
}
