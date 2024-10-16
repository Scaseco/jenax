package org.aksw.jenax.io.kryo.jena;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Serializer for {@link Expr} via {@link ExprUtils#fmtSPARQL(Expr)} and {@link ExprUtils#parse(String)}.
 * Avoid use. Does not preserve any custom expression classes.
 *
 * @author Claus Stadler
 */
public class ExprSerializerViaString extends Serializer<Expr> {
    @Override
    public void write(Kryo kryo, Output output, Expr obj) {
        String str = ExprUtils.fmtSPARQL(obj);
        output.writeString(str);
    }

    @Override
    public Expr read(Kryo kryo, Input input, Class<Expr> objClass) {
        String str = input.readString();
        Expr result = ExprUtils.parse(str);
        return result;
    }
}
