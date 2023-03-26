package org.aksw.jenax.io.kryo.jena;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.other.G;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;

/** Tests for Graph, Model, DatasetGraph and Dataset */
public class TestRdfSerializers {
    protected Kryo kryo;

    public static Graph createTestGraph() {
        String NS = "http://www.example.org/";
        Graph result = GraphFactory.createDefaultGraph();
        for (int i = 0; i < 10; ++i) {
            result.add(NodeFactory.createURI(NS + "s" + i), RDF.type.asNode(), OWL.Thing.asNode());
        }
        return result;
    }

    @Before
    public void before() {
        kryo = new Kryo();
        JenaKryoRegistratorLib.registerClasses(kryo);
    }

    @Test
    public void testDefaultGraph() {
        Graph graph = createTestGraph();
        KryoUtils.testRoundtrip(kryo, graph, (e, a) -> {
            Assert.assertTrue(e.isIsomorphicWith(a));
        });
    }

    @Test
    public void testDefaultModel() {
        Model model = ModelFactory.createDefaultModel();
        G.addInto(model.getGraph(), createTestGraph());
        KryoUtils.testRoundtrip(kryo, model, (e, a) -> {
            Assert.assertTrue(e.isIsomorphicWith(a));
        });
    }
}
