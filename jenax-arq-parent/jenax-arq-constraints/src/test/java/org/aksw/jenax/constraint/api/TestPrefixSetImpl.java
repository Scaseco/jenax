package org.aksw.jenax.constraint.api;

import org.aksw.jenax.constraint.util.PrefixSet;
import org.aksw.jenax.constraint.util.PrefixSetImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class TestPrefixSetImpl {

    @Test
    public void testIntersection() {
        PrefixSet a = PrefixSetImpl.create("aa", "b");

        // bb should get shortened to b
        a.intersect(PrefixSetImpl.create("a", "bb"));
        Assertions.assertEquals(PrefixSetImpl.create("a", "b"), a);


        // now b should be dropped
        a.intersect(PrefixSetImpl.create("a"));
        Assertions.assertEquals(PrefixSetImpl.create("a"), a);
    }

}
