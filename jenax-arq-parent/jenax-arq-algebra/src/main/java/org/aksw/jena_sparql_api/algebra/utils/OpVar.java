package org.aksw.jena_sparql_api.algebra.utils;

import java.util.Objects;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpExt;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.util.NodeIsomorphismMap;

/**
 * An operator that references a result set by key, such as a key of a cache entry.
 * The evaluation depends on the execution context or the executor
 */
// A class with a seemingly similar purpose exists as "OpExtKey" in LSQ - consolidate!
public class OpVar
    extends OpExt
{
    protected Var var;

    public OpVar(Var var) {
        super(OpVar.class.getSimpleName());
        this.var = var;
    }

    public Var getVar() {
        return var;
    }

    @Override
    public Op effectiveOp() {
        return null;
    }

    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt) {
        // ExprEvalException?
        throw new RuntimeException("This class requires an executor that can handle " + getClass().getName());
    }

    @Override
    public void outputArgs(IndentedWriter out, SerializationContext sCxt) {
        out.print(var);
    }

    @Override
    public int hashCode() {
        //int result = tag.hashCode() * 13 + Objects.hashCode(key) * 11;
        int result = Objects.hash(tag, var);
        return result;
    }

    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap) {
        return other instanceof OpVar && Objects.equals(var, ((OpVar)other).var);
    }

}