package org.aksw.jena_sparql_api.core.utils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;

import org.aksw.jenax.arq.util.exec.query.QueryExecutionAdapter;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.riot.resultset.ResultSetOnClose;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;

public class ResultSetUtils {
    public static ResultSet fromXml(InputStream xmlInputStream) {
        ResultSet rs = ResultSetFactory.fromXML(xmlInputStream);
        ResultSet result = wrap(rs, xmlInputStream);
        return result;
    }

    public static ResultSet wrap(ResultSet rs, AutoCloseable closeable) {
        QueryExecution dummy = new QueryExecutionAdapter() {
            @Override
            public void close() {
                try {
                    closeable.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        ResultSet result = new ResultSetOnClose(rs, dummy::close);
        return result;
    }


    public static ResultSet tripleIteratorToResultSet(Iterator<Triple> tripleIt, QueryExecution closeable) {
        Iterator<Binding> bindingIt = Iterators.transform(tripleIt, F_TripleToBinding.fn);
        QueryIterator queryIter = QueryIterPlainWrapper.create(bindingIt);
        List<String> varNames = Arrays.asList("s", "p", "o");
        ResultSet baseRs = ResultSetFactory.create(queryIter, varNames);
        ResultSet result = new ResultSetOnClose(baseRs, closeable::close);
        return result;
    }
}
