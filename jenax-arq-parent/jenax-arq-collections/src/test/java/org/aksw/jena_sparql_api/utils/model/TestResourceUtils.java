package org.aksw.jena_sparql_api.utils.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.aksw.jena_sparql_api.rdf.collections.NodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.rdf.collections.SetFromMappedPropertyValues;
import org.aksw.jenax.arq.util.triple.SetFromGraph;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class TestResourceUtils {


    @Test
    public void testUriStringMapper() {
        NodeMapper<String> m = NodeMappers.uriString;
        {
            boolean canMap = m.canMap(NodeFactory.createURI("http://example.org/foo"));
            Assertions.assertTrue(canMap);
        }

        {
            boolean canMap = m.canMap(NodeFactory.createLiteralString("http://example.org/foo"));
            Assertions.assertFalse(canMap);
        }
    }

    @Test
    public void testSetFromUriString() {

        Resource s = ResourceFactory.createResource("http://foo.bar/baz");

        Model finalExpectedModel = ModelFactory.createDefaultModel();
        s = s.inModel(finalExpectedModel);
        finalExpectedModel.add(s, RDF.type, OWL.Thing);
        finalExpectedModel.addLiteral(s, RDFS.label, 5);
        finalExpectedModel.add(s, RDFS.label, "label");

        Model model = ModelFactory.createDefaultModel();
        s = s.inModel(model);
        model.add(finalExpectedModel);
        model.add(s, RDFS.label, OWL.Class);

        SetFromMappedPropertyValues<String> actual = new SetFromMappedPropertyValues<>(s, RDFS.label, NodeMappers.uriString);
        Set<String> expected = new HashSet<>(Arrays.asList(OWL.Class.getURI()));
        Assertions.assertEquals(expected, actual);

        // Add via set view
        actual.add(RDFS.Class.getURI());
        expected.add(RDFS.Class.getURI());
        Assertions.assertEquals(expected, actual);

        // Add via resource/model
        s.addProperty(RDFS.label, RDF.Property);
        expected.add(RDF.Property.getURI());
        Assertions.assertEquals(expected, actual);

        // Clear the set
        actual.clear();
        expected.clear();
        Assertions.assertEquals(expected, actual);

        // Expect other triples to be untouched
        boolean sameModel = new SetFromGraph(finalExpectedModel.getGraph()).equals(new SetFromGraph(model.getGraph()));
        //boolean sameModel = finalExpectedModel.equals(model);
        if(!sameModel) {
            System.out.println("Expected Model:");
            RDFDataMgr.write(System.out, finalExpectedModel, RDFFormat.TURTLE_PRETTY);

            System.out.println();
            System.out.println("Actual Model:");
            RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);
        }

        Assertions.assertTrue(sameModel);
    }

    @Test
    public void testNodeMapperUriOrString() {
        NodeMapper<String> m = NodeMappers.DEFAULT_URI_OR_STRING;

        Assertions.assertTrue(m.canMap(NodeFactory.createURI("http://example.org")));
        Assertions.assertTrue(m.canMap(NodeFactory.createLiteralString("hi")));
        Assertions.assertTrue(m.canMap(NodeFactory.createLiteralString("there")));

        Assertions.assertFalse(m.canMap(NodeFactory.createLiteralLang("there", "en")));


        Assertions.assertFalse(m.toNode("hi").isURI());
        Assertions.assertTrue(m.toNode("http://foobar.org/baz").isURI());
        Assertions.assertFalse(m.toNode("http://foob ar.org/baz").isURI());
        Assertions.assertFalse(m.toNode("mailto://foo@bar.baz").isURI());
//		Assertions.assertTrue(m.toNode("mailto:foo@bar.baz").isURI());

    }


    @Test
    public void testNodeMapper() {

        NodeMapper<Long> m = NodeMappers.from(Long.class);

        {
            Node node = m.toNode(5l);
            boolean canMap = m.canMap(node);
            Assertions.assertEquals(true, canMap);

            Long value = m.toJava(node);
            Assertions.assertEquals(5l, value.longValue());
        }

        {
            Node node = NodeFactory.createLiteralDT("-3", TypeMapper.getInstance().getTypeByClass(Integer.class));
            boolean canMap = m.canMap(node);
            Assertions.assertEquals(true, canMap);
            Long value = m.toJava(node);
            Assertions.assertEquals(-3l, value.longValue());
        }

    }
}
