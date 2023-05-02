package org.aksw.jenax.io.kryo.jena;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/** Does not retain the original Binding type; the deserialized version is piped through a BindingBuilder. */
public class BindingSerializer
    extends Serializer<Binding>
{
    @Override
    public void write(Kryo kryo, Output output, Binding b) {
        // Not using the overhead of toMap anymore
        // Map<Var, Node> map = BindingUtils.toMap(object);
        // kryo.writeClassAndObject(output, map);
        int n = b.size();
        output.writeInt(n);
        b.forEach((v, node) -> {
            output.writeString(v.getName());
            kryo.writeClassAndObject(output, node);
        });
    }

    @Override
    public Binding read(Kryo kryo, Input input, Class<Binding> type) {
//        @SuppressWarnings("unchecked")
//        Map<Var, Node> map = (Map<Var, Node>)kryo.readClassAndObject(input);
        BindingBuilder builder = BindingFactory.builder();
        int n = input.readInt();
        for (int i = 0; i < n; ++i) {
            String varName = input.readString();
            Var var = Var.alloc(varName);
            Node node = (Node)kryo.readClassAndObject(input);
            builder.add(var, node);
        }
        Binding result = builder.build();
        return result;
    }
}
