package org.aksw.jenax.arq.util.binding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.aksw.jenax.arq.util.var.VarUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;


public class ResultSetUtils {

    /** Materialize a {@link ResultSet} into a {@link Table} */
    public static Table resultSetToTable(ResultSet rs) {
        List<Var> vars = Var.varList(rs.getResultVars());
        Table result = TableFactory.create(vars);
        while (rs.hasNext()) {
            Binding b = BindingFactory.copy(rs.nextBinding());
            result.addBinding(b);
        }

        return result;
    }

    public static List<Var> getVars(ResultSet rs) {
        List<Var> result = VarUtils.toList(rs.getResultVars());
        return result;
    }

    public static Node getNextNode(ResultSet rs, Var v) {
        Node result = null;

        if (rs.hasNext()) {
            Binding binding = rs.nextBinding();
            result = binding.get(v);
        }
        return result;
    }

    public static Optional<Node> tryGetNextNode(ResultSet rs, Var v) {
        Node node = getNextNode(rs, v);
        Optional<Node> result = Optional.ofNullable(node);
        return result;
    }

    public static RDFNode getNextRDFNode(ResultSet rs, Var v) {
        RDFNode result = null;
        if (rs.hasNext()) {
            QuerySolution qs = rs.next();
            String varName = v.getName();
            result = qs.get(varName);
        }
        return result;
    }

    public static Optional<RDFNode> tryGetNextRDFNode(ResultSet rs, Var v) {
        RDFNode node = getNextRDFNode(rs, v);
        Optional<RDFNode> result = Optional.ofNullable(node);
        return result;
    }

    public static Integer resultSetToInt(ResultSet rs, Var v) {
        Integer result = null;

        if (rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node node = binding.get(v);
            NodeValue nv = NodeValue.makeNode(node);
            result = nv.getInteger().intValue();

            // TODO Validate that the result actually is int.
            //result = node.getLiteral().
        }

        return result;
    }



    public static List<Binding> resultSetToList(ResultSet rs) {
        List<Binding> result = new ArrayList<>();
        while (rs.hasNext()) {
            Binding binding = rs.nextBinding();
            result.add(binding);
        }
        return result;
    }

    public static List<Node> resultSetToList(ResultSet rs, Var v) {
        List<Node> result = new ArrayList<Node>();
        while (rs.hasNext()) {
            Binding binding = rs.nextBinding();

            Node node = binding.get(v);
            result.add(node);
        }
        return result;
    }


    public static ResultSet create(List<String> varNames, Iterator<Binding> bindingIt) {
        QueryIterator queryIter = QueryIterPlainWrapper.create(bindingIt);

        ResultSet result = ResultSetFactory.create(queryIter, varNames);
        return result;
    }

    /** Create from vars (instead of var names) */
    public static ResultSet createUsingVars(Iterable<Var> vars, Iterator<Binding> bindingIt) {
        List<String> varNames = VarUtils.getVarNames(vars);
        ResultSet result = create(varNames, bindingIt);
        return result;
    }

    public static Multiset<QuerySolution> toMultisetQs(ResultSet rs) {
        Multiset<QuerySolution> result = HashMultiset.create();
        while(rs.hasNext()) {
            QuerySolution original = rs.next();

            QuerySolution wrapped = new QuerySolutionWithEquals(original);

            result.add(wrapped);
        }

        return result;
    }

    public static Multiset<Binding> toMultiset(ResultSet rs) {
        Multiset<Binding> result = HashMultiset.create();
        while(rs.hasNext()) {
            Binding original = rs.nextBinding();

            Binding wrapped = original;
            //QuerySolution wrapped = new QuerySolutionWithEquals(original);

            result.add(wrapped);
        }

        return result;
    }

}

