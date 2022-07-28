package org.aksw.jenax.arq.util.binding;

import java.util.Objects;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.exec.ResultSetAdapter;
import org.apache.jena.sparql.exec.RowSet;

/** Combines a table with a model */
public class ResultTable {
    protected Table table;
    protected Model model;

    public ResultTable(Table table, Model model) {
        super();
        this.table = table;
        this.model = model;
    }

    public Table getTable() {
        return table;
    }

    public Model getModel() {
        return model;
    }

    /** Create a new result set view */
    public ResultSet newResultSet() {
        RowSet rs = table.toRowSet();
        ResultSet result = new ResultSetAdapter(rs, model);
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(model, table);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ResultTable other = (ResultTable) obj;
        return Objects.equals(model, other.model) && Objects.equals(table, other.table);
    }
}
