package org.aksw.jenax.arq.util.binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.table.TableN;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.graph.NodeTransform;

public class TableUtils {

    public static ResultSet toResultSet(Table table) {
        return ResultSet.adapt(table.toRowSet());
    }

    public static Table createTable(List<Var> vars, Iterable<Binding> bindings) {
        return createTable(vars, bindings.iterator());
    }

    public static Table createTable(List<Var> vars, Iterator<Binding> bindings) {
        Table result = TableFactory.create(vars);
        bindings.forEachRemaining(result::addBinding);
        return result;
    }

    public static Table createTable(RowSet rs) {
        List<Var> vars = rs.getResultVars();
        return createTable(vars, rs);
    }

    public static Table createTable(ResultSet rs) {
        RowSet rowSet = RowSet.adapt(rs);
        return createTable(rowSet);
    }

    public static Table createTable(Var var, Iterable<Node> nodesIt) {
        return createTable(var, nodesIt);
    }

    public static Table createTable(Var var, Iterator<Node> nodesIt) {
        Table result = TableFactory.create(Arrays.asList(var));
        while (nodesIt.hasNext()) {
            Node node = nodesIt.next();
            result.addBinding(BindingFactory.binding(var, node));
        }
        return result;
    }

    @Deprecated /** There is NodeTransformLib.transform() for tables */
    public static Table applyNodeTransform(Table table, NodeTransform transform) {
        List<Var> oldVars = table.getVars();

        List<Var> newVars = new ArrayList<Var>(oldVars.size());
        for(Var o : oldVars) {
            Var n = (Var)transform.apply(o);
            newVars.add(n);
        }

        //List<Binding> newBindings = new ArrayList<Binding>(table.size());
        Table result = new TableN(newVars);

        Iterator<Binding> it = table.rows();
        while(it.hasNext()) {
            Binding o = it.next();

            Binding n = BindingUtils.transformKeys(o, transform);
            result.addBinding(n);
        }

        return result;
    }
}
