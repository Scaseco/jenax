package org.aksw.jenax.io.kryo.jena;

import org.apache.jena.atlas.lib.tuple.Tuple;
import org.apache.jena.atlas.lib.tuple.TupleFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class TupleSerializer<T>
    extends Serializer<Tuple<T>>
{
    protected Class<T> componentClass;

    public TupleSerializer(Class<T> componentClass) {
        super();
        this.componentClass = componentClass;
    }

    @Override
    public void write(Kryo kryo, Output output, Tuple<T> object) {
        T[] nodes = object.asArray(componentClass);
        kryo.writeClassAndObject(output, nodes);
    }

    @Override
    public Tuple<T> read(Kryo kryo, Input input, Class<Tuple<T>> type) {
        @SuppressWarnings("unchecked")
        T[] nodes = (T[])kryo.readClassAndObject(input);
        Tuple<T> result = TupleFactory.create(nodes);
        return result;
    }
}
