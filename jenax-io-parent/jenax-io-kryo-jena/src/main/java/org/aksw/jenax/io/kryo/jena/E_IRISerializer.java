package org.aksw.jenax.io.kryo.jena;

import java.util.function.BiFunction;

import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.Expr;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/** Needs special handling because of parser base */
public class E_IRISerializer<T extends E_IRI> // Also for E_URI
    extends Serializer<T>
{
    protected BiFunction<String, Expr, Expr> ctor;

    public E_IRISerializer(BiFunction<String, Expr, Expr> ctor) {
        super();
        this.ctor = ctor;
    }

    @Override
    public void write(Kryo kryo, Output output, T object) {
        output.writeString(object.getParserBase());
        kryo.writeClassAndObject(output, object.getArg());
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> type) {
        String parserBase = input.readString();
        Expr arg = (Expr)kryo.readClassAndObject(input);
        @SuppressWarnings("unchecked")
        T result = (T)ctor.apply(parserBase, arg);
        return result;
    }
}
