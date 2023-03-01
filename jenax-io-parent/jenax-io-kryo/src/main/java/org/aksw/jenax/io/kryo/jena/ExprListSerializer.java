package org.aksw.jenax.io.kryo.jena;

import java.util.List;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializer for {@link ExprList}.
 */
public class ExprListSerializer extends Serializer<ExprList> {
    @Override
    public void write(Kryo kryo, Output output, ExprList obj) {
        kryo.writeClassAndObject(output, obj.getList());
    }

    @Override
    public ExprList read(Kryo kryo, Input input, Class<ExprList> objClass) {
        @SuppressWarnings("unchecked")
        List<Expr> args = (List<Expr>)kryo.readClassAndObject(input);
        return new ExprList(args);
    }
}
