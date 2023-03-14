package net.sansa_stack.kryo.jena;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.aksw.jenax.io.kryo.jena.GenericNodeSerializerCustom;
import org.aksw.jenax.io.kryo.jena.JenaKryoRegistratorLib;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class TestCustomNodeSerializer {

    protected Kryo kryo;

    @Before
    public void before() {
        kryo = new Kryo();
        JenaKryoRegistratorLib.registerNodeSerializers(kryo, new GenericNodeSerializerCustom());
    }

    @Test
    public void test01() {
        testRoundtrip(kryo, Quad.defaultGraphNodeGenerated);
    }

    @Test
    public void test02() {
        testRoundtrip(kryo, NodeValue.makeInteger(123).asNode());
    }

    @Test
    public void test03() {
        testRoundtrip(kryo, NodeFactoryExtra.createLiteralNode("lex", null, "http://my.data/type"));
    }

    @Test
    public void test04() {
        testRoundtrip(kryo, RDF.type.asNode());
    }

    @Test
    public void test05() {
        testRoundtrip(kryo, NodeFactory.createURI("urn:my:urn"));
    }

    public static void testRoundtrip(Kryo kryo, Object expected) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Output out = new Output(baos)) {
            kryo.writeClassAndObject(out, expected);
            out.flush();
            try (ByteArrayInputStream bain = new ByteArrayInputStream(baos.toByteArray());
                    Input in = new Input(bain)) {
                Object actual = kryo.readClassAndObject(in);

                Assert.assertEquals(expected, actual);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
