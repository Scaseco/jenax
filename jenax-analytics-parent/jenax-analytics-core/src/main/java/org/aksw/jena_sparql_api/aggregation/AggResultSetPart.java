package org.aksw.jena_sparql_api.aggregation;

import java.util.List;
import java.util.Set;

import org.aksw.jenax.arq.util.var.VarUtils;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;

public class AggResultSetPart
    implements Agg<Table>
{
    private List<String> varNames;

    public AggResultSetPart(List<String> varNames) {
        this.varNames = varNames;
    }

    @Override
    public Acc<Table> createAccumulator() {
        Acc<Table> result = new AccTable(varNames);
        return result;
    }

    /**
     *
     */
    @Override
    public Set<Var> getDeclaredVars() {
        //return null;
        Set<Var> result = VarUtils.toSet(varNames);
        return result;
    }

}
