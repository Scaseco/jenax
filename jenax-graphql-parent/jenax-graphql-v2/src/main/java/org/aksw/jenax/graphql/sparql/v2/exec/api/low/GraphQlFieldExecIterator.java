package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriterInMemory;
import org.apache.jena.atlas.iterator.IteratorSlotted;
import org.apache.jena.graph.Node;

// FIXME: Idea: GraphQlExec.asIterator(gonProvider); (or have the gonProvider supplied at the building stage)
// FIXME: Somewhere we need an adapter/converter/transformer from RDF to JSON
public class GraphQlFieldExecIterator<T, K>
    extends IteratorSlotted<T>
{
    // protected GonProviderApi<T, K, Node> gonProvider;
    protected GraphQlFieldExec<K> exec;

    // protected ObjectNotationWriterViaGon<T, K, Node> writer;
    protected ObjectNotationWriterInMemory<T, K, Node> writer;

    protected T pendingItem;

    public GraphQlFieldExecIterator(GraphQlFieldExec<K> exec, ObjectNotationWriterInMemory<T, K, Node> writer) {
        super();
        this.exec = exec;
        this.writer = writer;
    }

    @Override
    protected T moveToNext() {
        return pendingItem;
    }

    @Override
    protected boolean hasMore() {
        boolean result;
        try {
            result = exec.sendNextItemToWriter(writer);
            pendingItem = writer.getProduct();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
