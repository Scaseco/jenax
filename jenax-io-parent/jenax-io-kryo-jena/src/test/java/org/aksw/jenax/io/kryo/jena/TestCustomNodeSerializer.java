package org.aksw.jenax.io.kryo.jena;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;


public class TestCustomNodeSerializer {

    static { JenaSystem.init(); }

    protected Kryo kryo;

    @Before
    public void before() {
        kryo = new Kryo();
        JenaKryoRegistratorLib.registerNodeSerializers(kryo, new GenericNodeSerializerCustom());
    }

    @Test
    public void test01() {
        KryoUtils.testRoundtrip(kryo, Quad.defaultGraphNodeGenerated);
    }

    @Test
    public void test02() {
        KryoUtils.testRoundtrip(kryo, NodeValue.makeInteger(123).asNode());
    }

// NodeFactoryExtra.createLiteralNode was deprecated with jena 4.4.0 and now raises NPE.
//    @Test
//    public void test03() {
//        KryoUtils.testRoundtrip(kryo, NodeFactoryExtra.createLiteralNode("lex", null, "http://my.data/type"));
//    }

    @Test
    public void test04() {
        KryoUtils.testRoundtrip(kryo, RDF.type.asNode());
    }

    @Test
    public void test05() {
        KryoUtils.testRoundtrip(kryo, NodeFactory.createURI("urn:my:urn"));
    }
}
