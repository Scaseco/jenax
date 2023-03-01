package org.aksw.jenax.io.kryo.jena;

import org.apache.commons.lang3.function.TriFunction;
import org.apache.jena.sparql.expr.E_IRI2;
import org.apache.jena.sparql.expr.Expr;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/** Needs special handling because of parser base */
public class E_IRI2Serializer<T extends E_IRI2> // Also for E_URI
    extends Serializer<T>
{
    protected TriFunction<Expr, String, Expr, Expr> ctor;

    public E_IRI2Serializer(TriFunction<Expr, String, Expr, Expr> ctor) {
        super();
        this.ctor = ctor;
    }

    @Override
    public void write(Kryo kryo, Output output, T object) {
        kryo.writeClassAndObject(output, object.getBaseExpr());
        output.writeString(object.getParserBase());
        kryo.writeClassAndObject(output, object.getRelExpr());
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> type) {
        Expr baseExpr = (Expr)kryo.readClassAndObject(input);
        String parserBase = input.readString();
        Expr relExpr = (Expr)kryo.readClassAndObject(input);
        @SuppressWarnings("unchecked")
        T result = (T)ctor.apply(baseExpr, parserBase, relExpr);
        return result;
    }
}
