package org.aksw.jena_sparql_api.sparql.ext.array;

import java.util.List;

import org.aksw.jena_sparql_api.sparql.ext.util.MoreQueryExecUtils;
import org.aksw.jenax.arq.datatype.RDFDatatypeNodeList;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sys.JenaSystem;
import org.junit.Assert;
import org.junit.Test;

public class TestSparqlLibArray {

    static { JenaSystem.init(); }

    @Test
    public void testArrayAggCollectNonNull() {
        Node actual = MoreQueryExecUtils.evalQueryToNode("SELECT (array:collect(?s) AS ?c) { VALUES (?s ?o) { (<urn:s1> UNDEF) (<urn:s2> <urn:o1>) }  }");
        Node expected = NodeFactory.createLiteral("<urn:s1> <urn:s2>", RDFDatatypeNodeList.INSTANCE);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testArrayAggCollectWithNull() {
        Node actual = MoreQueryExecUtils.evalQueryToNode("SELECT (array:collect(?o) AS ?c) { VALUES (?s ?o) { (<urn:s1> UNDEF) (<urn:s2> <urn:o1>) }  }");
        Node expected = NodeFactory.createLiteral("UNDEF <urn:o1>", RDFDatatypeNodeList.INSTANCE);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testArrayFnGetNonNull() {
        Node actual = MoreQueryExecUtils.evalExprToNode("array:get('UNDEF <urn:s>'^^rdf:array, 1)");
        Node expected = NodeFactory.createURI("urn:s");
        Assert.assertEquals(expected, actual);
    }

    @Test(expected = ExprEvalException.class)
    public void testArrayFnGetNull() {
        Node actual = MoreQueryExecUtils.evalExprToNode("array:get('UNDEF <urn:s>'^^rdf:array, 0)");
        Node expected = null;
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testArrayPfUnnest() {
        List<Node> actual = MoreQueryExecUtils.evalQueryToNodes("SELECT ?x { 'UNDEF <urn:s>'^^rdf:array array:unnest (?x ?i) }");
        @SuppressWarnings("unchecked")
        List<Node> expected = (List<Node>)NodeFactory.createLiteral("UNDEF <urn:s>", RDFDatatypeNodeList.INSTANCE).getLiteralValue();
        Assert.assertEquals(expected, actual);
    }


}
