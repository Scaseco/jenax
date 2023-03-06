package org.aksw.jenax.io.kryo.jena;

import java.util.List;
import java.util.function.Function;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializer for subclasses of {@link ExprFunction}. Mainly for use with ExprFunctionN.
 * ExprFunctions that take a fixed number of arguments should use serializers that avoid the List overhead.
 */
public class ExprFunctionNSerializer<T extends ExprFunction> extends Serializer<T> {
    protected Function<List<Expr>, T> ctor;

    public ExprFunctionNSerializer(Function<List<Expr>, T> ctor) {
        super();
        this.ctor = ctor;
    }

    @Override
    public void write(Kryo kryo, Output output, ExprFunction obj) {
        List<Expr> args = obj.getArgs();
        kryo.writeClassAndObject(output, args);
    }

    @Override
    public T read(Kryo kryo, Input input, Class<T> objClass) {
        @SuppressWarnings("unchecked")
        List<Expr> args = (List<Expr>)kryo.readClassAndObject(input);
        T result = ctor.apply(args);
        return result;
    }
}

