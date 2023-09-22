package org.aksw.jenax.arq.util.binding;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingLib;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 9/20/12
 *         Time: 2:30 PM
 */
public class QuerySolutionWithEquals
    extends QuerySolutionWrapper
{
    public QuerySolutionWithEquals(QuerySolution querySolution) {
        super(querySolution);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof QuerySolution)) return false;

        QuerySolution that = (QuerySolution) o;
        Binding thisBinding = asBinding();
        Binding thatBinding = QuerySolutionWrapper.asBinding(that);
        boolean result = BindingLib.equals(thisBinding, thatBinding);
        return  result;
    }

    @Override
    public int hashCode() {
        Binding binding = asBinding();
        int result = binding.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String result = asBinding().toString();
        return result;
    }

    public static Map<String, RDFNode> createMap(QuerySolution querySolution) {
        Map<String, RDFNode> result = new HashMap<>();
        Iterator<String> it = querySolution.varNames();
        while(it.hasNext()) {
            String varName = it.next();
            result.put(varName, querySolution.get(varName));
        }
        return result;
    }
}
