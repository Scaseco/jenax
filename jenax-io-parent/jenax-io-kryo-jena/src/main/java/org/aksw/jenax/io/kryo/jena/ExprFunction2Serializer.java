package org.aksw.jenax.io.kryo.jena;

import java.util.function.BiFunction;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction2;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class ExprFunction2Serializer<T extends ExprFunction2> extends Serializer<T> {
    protected BiFunction<Expr, Expr, T> ctor;

    public ExprFunction2Serializer(BiFunction<Expr, Expr, T> ctor) {
        super();
        this.ctor = ctor;
    }

    @Override
    public void write(Kryo kryo, Output output, ExprFunction2 obj) {
        kryo.writeClassAndObject(output, obj.getArg1());
        kryo.writeClassAndObject(output, obj.getArg2());
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> objClass) {
        Expr arg1 = (Expr)kryo.readClassAndObject(input);
        Expr arg2 = (Expr)kryo.readClassAndObject(input);
        T result = ctor.apply(arg1, arg2);
        return result;
    }
}

