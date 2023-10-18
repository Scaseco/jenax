package org.aksw.jenax.arq.util.expr;

import java.util.Set;

import org.aksw.jenax.arq.util.var.Vars;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Assert;
import org.junit.Test;


public class TestDnfUtils {
    @Test
    public void testFixedVars01() {
        Set<Var> actual = DnfUtils.fixedVars(ExprUtils.parse("!bound(?x) || (?a < ?b)"));
        Assert.assertEquals(Set.of(), actual);
    }

    @Test
    public void testFixedVars02() {
        // TODO Maybe fixedVars should return a list of sets of variables.
        // if any variable in a set was fixed it would imply that all other variables in that set were fixed as well
        Set<Var> actual = DnfUtils.fixedVars(ExprUtils.parse("(?a < ?b) || (?x > ?y)"));
        Assert.assertEquals(Set.of(), actual);
    }

    @Test
    public void testFixedVars03() {
        Set<Var> actual = DnfUtils.fixedVars(ExprUtils.parse("(?a < ?b) && (?x > ?y)"));
        Assert.assertEquals(Set.of(Vars.a, Vars.b, Vars.x, Vars.y), actual);
    }

    @Test
    public void testFixedVars04() {
        Set<Var> actual = DnfUtils.fixedVars(ExprUtils.parse("(?a < ?b) && !bound(?x)"));
        Assert.assertEquals(Set.of(Vars.a, Vars.b), actual);
    }

    @Test
    public void testFixedVars05() {
        Set<Var> actual = DnfUtils.fixedVars(ExprUtils.parse("(?a < ?b) && coalesce(?x, ?y, ?z) = ?a"));
        // Actually ?a could be fixed as well
        Assert.assertEquals(Set.of(Vars.b), actual);
    }
}
