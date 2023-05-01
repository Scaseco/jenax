package org.aksw.jenax.io.kryo.jena;

import java.util.function.Function;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction1;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ExprFunction1Serializer<T extends ExprFunction1> extends Serializer<T> {
    protected Function<Expr, T> ctor;

    public ExprFunction1Serializer(Function<Expr, T> ctor) {
        super();
        this.ctor = ctor;
    }

    @Override
    public void write(Kryo kryo, Output output, ExprFunction1 obj) {
        kryo.writeClassAndObject(output, obj.getArg());
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> objClass) {
        Expr arg = (Expr)kryo.readClassAndObject(input);
        T result = ctor.apply(arg);
        return result;
    }
}
