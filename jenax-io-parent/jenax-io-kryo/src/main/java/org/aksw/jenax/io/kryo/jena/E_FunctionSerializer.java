package org.aksw.jenax.io.kryo.jena;

import java.util.List;
import java.util.function.BiFunction;

import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/** Needs special handling because of parser base */
public class E_FunctionSerializer<T extends E_Function>
    extends Serializer<T>
{
    protected BiFunction<String, ExprList, T> ctor;

    public E_FunctionSerializer(BiFunction<String, ExprList, T> ctor) {
        super();
        this.ctor = ctor;
    }

    @Override
    public void write(Kryo kryo, Output output, T object) {
        output.writeString(object.getFunctionIRI());
        kryo.writeClassAndObject(output, object.getArgs());
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> type) {
        String functionIRI = input.readString();
        @SuppressWarnings("unchecked")
        List<Expr> args = (List<Expr>)kryo.readClassAndObject(input);
        ExprList el = new ExprList(args);
        T result = ctor.apply(functionIRI, el);
        return result;
    }
}
