package org.aksw.jenax.io.kryo.jena;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Kryo serializer for {@link VarExprList}.
 *
 * @author Claus Stadler
 */
public class VarExprListSerializer extends Serializer<VarExprList> {
    @Override
    public void write(Kryo kryo, Output output, VarExprList vel) {
        int n = vel.size();
        output.writeInt(n);
        vel.forEachVarExpr((v, e) -> {
            // output.writeString(v.getName());
            kryo.writeClassAndObject(output, v);
            kryo.writeClassAndObject(output, e);
        });
    }

    @Override
    public VarExprList read(Kryo kryo, Input input, Class<VarExprList> objClass) {
        VarExprList result = new VarExprList();
        int n = input.readInt();
        for (int i = 0; i < n; ++i) {
            // Var var = Var.alloc(input.readString());
            Var var = (Var)kryo.readClassAndObject(input);
            Expr expr = (Expr)kryo.readClassAndObject(input);
            if (expr == null) {
                result.add(var);
            } else {
                result.add(var, expr);
            }
        }
        return result;
    }
}
