package org.aksw.jenax.sparql.algebra.walker;

import java.util.Map.Entry;

import org.aksw.commons.path.core.Path;
import org.aksw.jenax.constraint.api.ConstraintRow;
import org.aksw.jenax.sparql.algebra.optimize.TrackingTransformConditionalFunctionInversion;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.junit.Assert;
import org.junit.Test;


public class TrackingWalkerTests {

    /** Expect the filter condition to be rewritten appropriately */
    @Test
    public void testFunctionInversion() {
         Query query = QueryFactory.create(String.join("\n",
                 "SELECT * {",
                 "  ?s ?p ?o",
                 "  FILTER(STR(?p) = 'urn:example:foobar')",
                 "}"));

         Query expected = QueryFactory.create(String.join("\n",
                 "SELECT * {",
                 "  ?s ?p ?o",
                 "  FILTER(?p = <urn:example:foobar>)",
                 "}"));

        Op op = Algebra.compile(query);
        Tracker<ConstraintRow> pathState = Tracker.create(op);
        Op afterOp = TrackingTransformer.transform(pathState, TrackingTransformConditionalFunctionInversion::new);

        Query actual = OpAsQuery.asQuery(afterOp);

        Assert.assertEquals(expected, actual);

//        System.out.println(actual);
//
//        for (Entry<Path<String>, Op> e : pathState.getPathToOp().entrySet()) {
//            System.out.println(e.getKey());
//            System.out.println("---------------------------");
//            System.out.println(e.getValue());
//
//            System.out.println();
//        }
    }
}
