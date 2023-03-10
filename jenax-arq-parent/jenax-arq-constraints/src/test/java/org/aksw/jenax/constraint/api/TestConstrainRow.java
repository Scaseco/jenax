package org.aksw.jenax.constraint.api;

import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.constraint.api.CBinding;
import org.aksw.jenax.constraint.impl.CBindingMap;
import org.aksw.jenax.constraint.util.ConstraintDerivations;
import org.apache.jena.sparql.core.Quad;
import org.junit.Test;

public class TestConstrainRow {

    @Test
    public void test() {
        CBinding cr = CBindingMap.create();

        Quad quad = Quad.create(Vars.g, Vars.s, Vars.p, Vars.o);

        ConstraintDerivations.deriveConstraints(cr, quad);

        System.out.println(cr);
    }
}
