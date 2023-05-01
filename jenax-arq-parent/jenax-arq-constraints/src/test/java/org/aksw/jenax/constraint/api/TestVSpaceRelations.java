package org.aksw.jenax.constraint.api;

import org.aksw.commons.algebra.allen.AllenRelation;
import org.aksw.jenax.constraint.impl.RdfTermProfiles;
import org.junit.Assert;
import org.junit.Test;

public class TestVSpaceRelations {

    @Test
    public void test() {
        // VSpace x = RdfTermProfiles.forPrefix("rdf");
        VSpace x = RdfTermProfiles.forIriPrefix("rdfs")
                .stateUnion(RdfTermProfiles.forStringPrefix("rdf"));

        VSpace y = RdfTermProfiles.forIriPrefix("rdfq");
        //VSpace y = RdfTermProfiles.forStringPrefix("rdg");
        AllenRelation r = x.relateTo(y);
        System.out.println(r);
    }

    /** IRIs come before strings */
    @Test
    public void test_before_01() {
        VSpace x = RdfTermProfiles.forIriPrefix("rdf");
        VSpace y = RdfTermProfiles.forStringPrefix("rdf");
        AllenRelation actual = x.relateTo(y);
        Assert.assertEquals(actual, AllenRelation.BEFORE);
    }

    @Test
    public void test_meets_01() {
        VSpace x = RdfTermProfiles.forStringPrefix("rdf");
        VSpace y = RdfTermProfiles.forStringPrefix("rdg");
        AllenRelation actual = x.relateTo(y);
        Assert.assertEquals(actual, AllenRelation.MEETS);
    }
}
