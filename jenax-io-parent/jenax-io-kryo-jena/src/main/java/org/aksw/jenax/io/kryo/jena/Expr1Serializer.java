package org.aksw.jenax.io.kryo.jena;

import java.util.function.Function;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class Expr1Serializer<I, O>
    extends Serializer<I>
{
    protected Function<I, O> getArg;
    protected Function<O, I> ctor;

    public Expr1Serializer(Function<I, O> getArg, Function<O, I> ctor) {
        super();
        this.getArg = getArg;
        this.ctor = ctor;
    }

    @Override
    public void write(Kryo kryo, Output output, I obj) {
        O arg = getArg.apply(obj);
        kryo.writeClassAndObject(output, arg);
    }

    @Override
    public I read(Kryo kryo, Input input, Class<I> objClass) {
        @SuppressWarnings("unchecked")
        O arg = (O)kryo.readClassAndObject(input);
        I result = ctor.apply(arg);
        return result;
    }
}
