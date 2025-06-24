package org.aksw.jenax.dataaccess.sparql.exec.query;

import java.util.Iterator;

import org.aksw.jenax.arq.util.binding.BindingMapper;
import org.aksw.jenax.arq.util.binding.BindingMapperQuad;
import org.aksw.jenax.arq.util.binding.BindingMapperTriple;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.IteratorCloseable;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;

public class QueryExecUtils {
    public static IteratorCloseable<Binding> select(QueryExecFactoryQuery qef, Query query) {
        QueryExec qe = qef.create(query);
        RowSet rs = qe.select();
        IteratorCloseable<Binding> result = Iter.onClose(rs, rs::close);
        return result;
    }

    public static IteratorCloseable<Triple> findTriples(QueryExecFactoryQuery qef, Node s, Node p, Node o) {
        Triple triple = Triple.create(s, p, o);
        Query query = QueryGenerationUtils.createQueryTriple(triple);
        BindingMapper<Triple> mapper = new BindingMapperTriple(triple);
        IteratorCloseable<Triple> result = QueryExecUtils.execMapped(qef, query, mapper);
        return result;
    }

    public static IteratorCloseable<Quad> findQuads(QueryExecFactoryQuery qef, Node g, Node s, Node p, Node o) {
        Quad quad = new Quad(g, s, p, o);
        Query query = QueryGenerationUtils.createQueryQuad(quad);
        BindingMapper<Quad> mapper = new BindingMapperQuad(quad);
        IteratorCloseable<Quad> result = QueryExecUtils.execMapped(qef, query, mapper);
        return result;
    }

    /** TODO Cleanup: This method has a duplicate in QueryExecutionUtils */
    public static <T> IteratorCloseable<T> execMapped(QueryExecFactoryQuery qef, Query query, BindingMapper<T> bindingMapper) {
        QueryExec qe = qef.create(query);
        Iterator<Binding> itBinding = qe.select();

        long id[] = {0};
        IteratorCloseable<T> result = (IteratorCloseable<T>)Iter.map(itBinding, b -> bindingMapper.apply(b, id[0]++));
        return result;
    }
}
