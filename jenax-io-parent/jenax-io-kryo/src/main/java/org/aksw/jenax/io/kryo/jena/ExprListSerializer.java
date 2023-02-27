package org.aksw.jenax.io.kryo.jena;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.ExprUtils;

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
        output.writeInt(obj.size());
        for (Expr expr : obj) {
            String str = ExprUtils.fmtSPARQL(expr);
            output.writeString(str);
        }
    }

    @Override
    public ExprList read(Kryo kryo, Input input, Class<ExprList> objClass) {
        int n = input.read();
        List<Expr> list = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            String str = input.readString();
            Expr item = ExprUtils.parse(str);
            list.add(item);
        }
        return new ExprList(list);
    }
}
