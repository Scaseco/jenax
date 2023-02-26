package org.aksw.jenax.io.kryo.jena;

import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SortConditionSerializer extends Serializer<SortCondition> {
    @Override
    public void write(Kryo kryo, Output output, SortCondition obj) {
        output.writeString(ExprUtils.fmtSPARQL(obj.getExpression()));
        output.writeInt(obj.getDirection());
    }

    @Override
    public SortCondition read(Kryo kryo, Input input, Class<SortCondition> objClass) {
        String exprStr = input.readString();
        int direction = input.readInt();
        Expr expr = ExprUtils.parse(exprStr);
        SortCondition result = new SortCondition(expr, direction);
        return result;
    }
}
