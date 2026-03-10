package org.aksw.jenax.io.kryo.jena;

import java.util.function.BiFunction;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import org.apache.jena.sparql.expr.E_OneOfBase;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;

/**
 * Serializer for subclasses of {@link ExprFunction}. Mainly for use with ExprFunctionN.
 * ExprFunctions that take a fixed number of arguments should use serializers that avoid the List overhead.
 */
public class E_OneOfBaseSerializer<T extends E_OneOfBase> extends Serializer<T> {
    protected BiFunction<Expr, ExprList, T> ctor;

    public E_OneOfBaseSerializer(BiFunction<Expr, ExprList, T> ctor) {
        super();
        this.ctor = ctor;
    }

    @Override
    public void write(Kryo kryo, Output output, T expr) {
        kryo.writeClassAndObject(output, expr.getLHS());
        kryo.writeClassAndObject(output, expr.getRHS());
        // kryo.writeClassAndObject(output, expr.getArgs());
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> objClass) {
        Expr expr  = (Expr)kryo.readClassAndObject(input);
        // List<Expr> args = (List<Expr>)kryo.readClassAndObject(input);
        // ExprList el = new ExprList(args);
        ExprList el = (ExprList)kryo.readClassAndObject(input);
        T result = ctor.apply(expr, el);
        return result;
    }
}

