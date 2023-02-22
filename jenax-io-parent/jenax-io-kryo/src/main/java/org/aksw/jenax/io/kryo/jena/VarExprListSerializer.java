package org.aksw.jenax.io.kryo.jena;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

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
    public void write(Kryo kryo, Output output, VarExprList obj) {
        Map<Var, String> serializableMap = obj.getExprs().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> {
                    Expr x = e.getValue();
                    return x == null ? null : ExprUtils.fmtSPARQL(x);
                }));

        kryo.writeClassAndObject(output, obj.getVars());
        kryo.writeClassAndObject(output, serializableMap);
    }

    @Override
    public VarExprList read(Kryo kryo, Input input, Class<VarExprList> objClass) {
        @SuppressWarnings("unchecked")
        List<Var> vars = (List<Var>) kryo.readClassAndObject(input);
        @SuppressWarnings("unchecked")
        Map<Var, String> map = (Map<Var, String>) kryo.readClassAndObject(input);

        VarExprList result = new VarExprList();
        vars.forEach(v -> {
            String e = map.get(v);
            if (e == null) {
                result.add(v);
            } else {
                Expr x = ExprUtils.parse(e);
                result.add(v, x);
            }
        });

        return result;
    }
}
