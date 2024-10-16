package org.aksw.jenax.graphql.sparql.v2.exec.api.high;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;

import org.aksw.jenax.graphql.sparql.v2.exec.api.low.GraphQlExecCore;
import org.aksw.jenax.graphql.sparql.v2.exec.api.low.GraphQlFieldExec;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.apache.jena.graph.Node;

/**
 * Adapter to wrap the execution's intended for individual nodes as one for an overall document.
 * (Perhaps this interface should be renamed to GraphQlDocumentExec or maybe it can even be removed?)
 */
public class GraphQlExec<K>
    implements GraphQlExecCore
{
    protected GraphQlFieldExec<K> delegate;

    public GraphQlExec(GraphQlFieldExec<K> delegate) {
        super();
        this.delegate = Objects.requireNonNull(delegate);
    }

    public GraphQlFieldExec<K> getDelegate() {
        return delegate;
    }

    public boolean sendNextItemToWriter(ObjectNotationWriter<K, Node> writer) throws IOException {
        return delegate.sendNextItemToWriter(writer);
    }

    public void writeExtensions(ObjectNotationWriter<K, Node> writer, Function<String, K> stringToKey) throws IOException {
        delegate.writeExtensions(writer, stringToKey);
    }

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
