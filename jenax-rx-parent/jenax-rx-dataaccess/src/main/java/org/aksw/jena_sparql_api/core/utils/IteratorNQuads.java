package org.aksw.jena_sparql_api.core.utils;

import org.aksw.commons.collections.SinglePrefetchIterator;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ResultSetCloseable;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;

public class IteratorNQuads
    extends SinglePrefetchIterator<Quad>
{
    private ResultSetCloseable rs;

    public IteratorNQuads(ResultSetCloseable rs) {
        this.rs = rs;
    }

    @Override
    protected Quad prefetch() throws Exception {
        if(!rs.hasNext()) {
            return finish();
        }

        Binding binding = rs.nextBinding();

        Node g = binding.get(QueryExecutionUtils.vg);
        Node s = binding.get(QueryExecutionUtils.vs);
        Node p = binding.get(QueryExecutionUtils.vp);
        Node o = binding.get(QueryExecutionUtils.vo);

        Quad result = new Quad(g, s, p, o);
        return result;
    }

    @Override
    public void close() {
        try {
            rs.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}