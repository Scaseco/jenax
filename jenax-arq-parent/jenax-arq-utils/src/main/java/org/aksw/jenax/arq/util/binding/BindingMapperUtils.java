package org.aksw.jenax.arq.util.binding;

import java.util.Iterator;
import java.util.function.Function;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;


public class BindingMapperUtils {

//    public static <T> Iterator<T> execMapped(Function<Query, QueryExec> qef, Query query, BindingMapper<T> bindingMapper) {
//        // QueryExecution qe = qef.createQueryExecution(query);
//        // ResultSet rs = qe.execSelect();
//
//        QueryExec qe = qef.apply(query);
//        Iterator<Binding> itBinding = qe.select();
//        // Function<Binding, T> fn = FunctionBindingMapper.create(bindingMapper);
//
//        // FIXME Adapt the mapper to increment the binding id.
//        Iterator<T> result = Iter.map(itBinding, b -> bindingMapper.apply(b, null));
//        return result;
//    }

}
