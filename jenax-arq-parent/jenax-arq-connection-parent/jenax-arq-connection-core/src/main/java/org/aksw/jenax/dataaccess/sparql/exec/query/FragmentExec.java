package org.aksw.jenax.dataaccess.sparql.exec.query;

import java.util.Iterator;

import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

public class FragmentExec {
    public static Iterator<Node> execNode(QueryExecFactoryQuery qef, Fragment1 fragment) {
        Var var = fragment.getVar();
        Query query = fragment.toQuery();
        Iterator<Binding> it1 = QueryExecUtils.select(qef, query);
        Iterator<Node> it2 = Iter.map(it1, b -> b.get(var));
        return it2;
    }
}
