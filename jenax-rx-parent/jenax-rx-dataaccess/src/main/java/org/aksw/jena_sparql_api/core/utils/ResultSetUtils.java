package org.aksw.jena_sparql_api.core.utils;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.core.ResultSetCloseable;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

import com.google.common.collect.Iterators;


public class ResultSetUtils {
    public static ResultSetCloseable tripleIteratorToResultSet(Iterator<Triple> tripleIt, Closeable closeable) {
        Iterator<Binding> bindingIt = Iterators.transform(tripleIt, F_TripleToBinding.fn);
        QueryIterator queryIter = QueryIterPlainWrapper.create(bindingIt);
        List<String> varNames = Arrays.asList("s", "p", "o");
        ResultSet baseRs = ResultSetFactory.create(queryIter, varNames);
        ResultSetCloseable result = new ResultSetCloseable(baseRs, closeable);
        return result;
    }
}
