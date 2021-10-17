package org.aksw.jenax.arq.aggregation;

import java.util.List;

import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class AccTable
    implements Acc<Table>
{
    private Table value;

    public AccTable(List<String> varNames) {
        this.value = TableFactory.create(Var.varList(varNames));
    }

    @Override
    public void accumulate(Binding binding) {
        value.addBinding(binding);
    }

    @Override
    public Table getValue() {
        return value;
    }
}
