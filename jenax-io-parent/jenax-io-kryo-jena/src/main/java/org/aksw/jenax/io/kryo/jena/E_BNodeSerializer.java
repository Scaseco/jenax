package org.aksw.jenax.io.kryo.jena;

import java.util.List;

import org.apache.jena.ext.com.google.common.base.Preconditions;
import org.apache.jena.sparql.expr.E_BNode;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.sse.Tags;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class E_BNodeSerializer
    extends Serializer<ExprFunction>
{
    public E_BNodeSerializer() {
        super();
    }

    @Override
    public void write(Kryo kryo, Output output, ExprFunction func) {
        Preconditions.checkArgument(Tags.tagBNode.equals(func.getFunctionSymbol().getSymbol()), "Not a E_BNode expression");
        kryo.writeClassAndObject(output, func.getArgs());
    }

    @Override
    public ExprFunction read(Kryo kryo, Input input, Class<ExprFunction> type) {
        @SuppressWarnings("unchecked")
        List<Expr> args = (List<Expr>)kryo.readClassAndObject(input);
        int n = args.size();
        Preconditions.checkState(n < 2, "E_BNode with more than 1 argument");
        Expr expr = n == 0
                ? E_BNode.create()
                : E_BNode.create(args.get(0));
        ExprFunction result = (ExprFunction)expr;
        return result;
    }
}
