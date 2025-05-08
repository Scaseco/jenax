package org.aksw.jena_sparql_api.mapper;

import java.util.Iterator;
import java.util.function.Function;

import org.aksw.jenax.arq.aggregation.BindingMapper;
import org.aksw.jenax.arq.aggregation.FunctionBindingMapper;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.RowSet;

import com.google.common.collect.Iterators;


public class BindingMapperUtils {

    public static <T> Iterator<T> execMapped(QueryExecutionFactory qef, Query query, BindingMapper<T> bindingMapper) {
        QueryExecution qe = qef.createQueryExecution(query);
        ResultSet rs = qe.execSelect();

        Iterator<Binding> itBinding = RowSet.adapt(rs);
        Function<Binding, T> fn = FunctionBindingMapper.create(bindingMapper);

        Iterator<T> result = Iterators.transform(itBinding, fn::apply);
        return result;
    }

}
