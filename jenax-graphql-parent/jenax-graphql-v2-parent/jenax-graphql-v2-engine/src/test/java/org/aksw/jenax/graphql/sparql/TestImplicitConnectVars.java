package org.aksw.jenax.graphql.sparql;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.aksw.jenax.graphql.sparql.v2.util.ElementUtils;
import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.lang.ParserARQ;
import org.apache.jena.sparql.syntax.Element;
import org.junit.Assert;
import org.junit.Test;

public class TestImplicitConnectVars {

    // @Test
    public void testX() {
        Op a = Algebra.compile(ParserARQ.parseElement("""
                {
                SELECT * {
                {
                ?s ?p ?o .
                FILTER(?p NOT IN (<rdfs:label>))
                }
                # Auto-derive property cardinalities from all data
                { SERVICE <cache:> {
                  { SELECT ?p (MAX(?c) AS ?pc) {
                    SELECT ?x ?p (COUNT(*) AS ?c) {
                     ?x ?p ?z
                    } GROUP BY ?x ?p
                  } GROUP BY ?p }
                }
                }
                }}
      """));

        Op b = Rename.renameVars(a, List.of());

        System.out.println(OpVars.mentionedVarsByPosition(b));
        System.out.println(OpVars.visibleVars(b));
    }

    @Test
    public void test01() {
        test("{ ?s a <urn:Foo> }", "s");
    }

    @Test
    public void test02() {
        test("{ ?s a <urn:Foo> BIND('foo' AS ?o) }", (String[])null);
    }

    @Test
    public void test11() {
        test("{ ?s a ?o }", "s", "o");
    }

    @Test
    public void test12() {
        test("{ ?s ?p ?o }", "s", "o");
    }

    @Test
    public void test13() {
        test("{ ?s <urn:p1>/<urn:p2> ?o }", "s", "o");
    }

    @Test
    public void test100() {
        test("{ ?a ?b ?c . ?c ?d ?e . }", (String[])null);
    }


    @Test
    public void test10() {
        test("{ GRAPH ?g { ?s ?p ?o } }", "s", "o");
    }

    @Test
    public void test200() {
        test("{ ?s ?p ?o . FILTER(langMatches(?o, 'en')) }", "s", "o");
    }


    public static void test(String eltStr, String ... expectedVarNames) {
        List<Var> expected = expectedVarNames == null ? null : Var.varList(Arrays.asList(expectedVarNames));
        Element elt = ParserARQ.parseElement(eltStr);
        List<Var> actual = ElementUtils.inferConnecVars(elt);
        Assert.assertEquals(expected, actual);
    }

}
