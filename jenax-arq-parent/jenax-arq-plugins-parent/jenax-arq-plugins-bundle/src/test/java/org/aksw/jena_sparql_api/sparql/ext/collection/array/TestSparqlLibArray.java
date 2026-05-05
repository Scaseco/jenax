package org.aksw.jena_sparql_api.sparql.ext.collection.array;

import java.util.List;

import org.aksw.jena_sparql_api.sparql.ext.util.MoreQueryExecUtils;
import org.aksw.jenax.arq.datatype.RDFDatatypeNodeList;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sys.JenaSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestSparqlLibArray {

    static { JenaSystem.init(); }

    @Test
    public void testArrayOf() {
        Node actual = MoreQueryExecUtils.INSTANCE.evalQueryToNode("SELECT (array:of(1, 'hi') AS ?x) { }");
        Node expected = NodeFactory.createLiteralByValue(RDFDatatypeNodeList.INSTANCE.parse("1 'hi'"), RDFDatatypeNodeList.INSTANCE);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testArrayExplode() {
        Binding actual = MoreQueryExecUtils.INSTANCE.evalQueryToBinding("SELECT ?a ?b { BIND(array:of(1, 'hi') AS ?x) ?x array:explode(?a ?b) }");
        Binding expected = MoreQueryExecUtils.INSTANCE.evalQueryToBinding("SELECT (1 AS ?a) ('hi' AS ?b) { }");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testArrayAggCollectNonNull() {
        Node actual = MoreQueryExecUtils.INSTANCE.evalQueryToNode("SELECT (array:collect(?s) AS ?c) { VALUES (?s ?o) { (<urn:s1> UNDEF) (<urn:s2> <urn:o1>) }  }");
        Node expected = NodeFactory.createLiteralDT("<urn:s1> <urn:s2>", RDFDatatypeNodeList.INSTANCE);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testArrayAggCollectWithNull() {
        Node actual = MoreQueryExecUtils.INSTANCE.evalQueryToNode("SELECT (array:collect(?o) AS ?c) { VALUES (?s ?o) { (<urn:s1> UNDEF) (<urn:s2> <urn:o1>) }  }");
        Node expected = NodeFactory.createLiteralDT("UNDEF <urn:o1>", RDFDatatypeNodeList.INSTANCE);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testArrayFnGetNonNull() {
        Node actual = MoreQueryExecUtils.INSTANCE.evalExprToNode("array:get('UNDEF <urn:s>'^^norse:array, 1)");
        Node expected = NodeFactory.createURI("urn:s");
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testArrayFnGetNull() {
        assertThrows(ExprEvalException.class, () -> {
            Node actual = MoreQueryExecUtils.INSTANCE.evalExprToNode("array:get('UNDEF <urn:s>'^^norse:array, 0)");
            Node expected = null;
            Assertions.assertEquals(expected, actual);
        });
    }

    @Test
    public void testArrayPfUnnest() {
        List<Node> actual = MoreQueryExecUtils.INSTANCE.evalQueryToNodes("SELECT ?x { 'UNDEF <urn:s>'^^norse:array array:unnest (?x ?i) }");
        @SuppressWarnings("unchecked")
        List<Node> expected = (List<Node>)NodeFactory.createLiteralDT("UNDEF <urn:s>", RDFDatatypeNodeList.INSTANCE).getLiteralValue();
        Assertions.assertEquals(expected, actual);
    }
}
