package org.aksw.jenax.io.kryo.jena;

import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.util.ModelUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class RDFNodeSerializer<T extends RDFNode>
        extends Serializer<T> {

    protected Function<? super RDFNode, T> fn;

    public RDFNodeSerializer(Function<? super RDFNode, T> fn) {
        super();
        this.fn = fn;
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> clazz) {
        Node node = (Node)kryo.readClassAndObject(input);
        Model model = (Model)kryo.readClassAndObject(input);
        RDFNode rdfNode = ModelUtils.convertGraphNodeToRDFNode(node, model);
        T result = fn.apply(rdfNode);
        return result;
    }

    @Override
    public void write(Kryo kryo, Output output, T rdfNode) {
        Node node = rdfNode.asNode();
        Model model = rdfNode.getModel();
        kryo.writeClassAndObject(output, node);
        kryo.writeClassAndObject(output, model);
    }
}
