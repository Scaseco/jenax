package org.aksw.jenax.io.kryo.jena;

import org.apache.commons.lang3.function.TriFunction;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction3;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


public class ExprFunction3Serializer<T extends ExprFunction3> extends Serializer<T> {
    protected TriFunction<Expr, Expr, Expr, T> ctor;

    public ExprFunction3Serializer(TriFunction<Expr, Expr, Expr, T> ctor) {
        super();
        this.ctor = ctor;
    }

    @Override
    public void write(Kryo kryo, Output output, ExprFunction3 obj) {
        kryo.writeClassAndObject(output, obj.getArg1());
        kryo.writeClassAndObject(output, obj.getArg2());
        kryo.writeClassAndObject(output, obj.getArg3());
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> objClass) {
        Expr arg1 = (Expr)kryo.readClassAndObject(input);
        Expr arg2 = (Expr)kryo.readClassAndObject(input);
        Expr arg3 = (Expr)kryo.readClassAndObject(input);
        T result = ctor.apply(arg1, arg2, arg3);
        return result;
    }
}
