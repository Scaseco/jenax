package org.aksw.jenax.io.kryo.jena;

import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.expr.Expr;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class SortConditionSerializer extends Serializer<SortCondition> {
    @Override
    public void write(Kryo kryo, Output output, SortCondition obj) {
        kryo.writeClassAndObject(output, obj.getExpression());
        output.writeInt(obj.getDirection());
    }

    @Override
    public SortCondition read(Kryo kryo, Input input, Class<SortCondition> objClass) {
        Expr expr = (Expr)kryo.readClassAndObject(input);
        int direction = input.readInt();
        SortCondition result = new SortCondition(expr, direction);
        return result;
    }
}
