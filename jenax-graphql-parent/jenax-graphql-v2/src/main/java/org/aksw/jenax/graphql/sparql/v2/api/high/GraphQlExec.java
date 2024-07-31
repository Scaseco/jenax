package org.aksw.jenax.graphql.sparql.v2.api.high;

import java.io.IOException;
import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.api.low.GraphQlExecCore;
import org.aksw.jenax.graphql.sparql.v2.api.low.GraphQlFieldExec;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.apache.jena.graph.Node;

public class GraphQlExec<K>
    implements GraphQlExecCore
{
    protected GraphQlFieldExec<K> delegate;

    public GraphQlExec(GraphQlFieldExec<K> delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    public boolean sendNextItemToWriter(ObjectNotationWriter<K, Node> writer) throws IOException {
        return delegate.sendNextItemToWriter(writer);
    }

//    public boolean sendNextItemToWriter(ObjectNotationWriter<String, Object> jsonWriter) throws IOException {
//
//    }

    @Override
    public boolean isSingle() {
        return delegate.isSingle();
    }

    @Override
    public void abort() {
        delegate.abort();
    }

    @Override
    public void close() {
        delegate.close();
    }
}
