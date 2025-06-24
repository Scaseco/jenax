package org.aksw.jenax.arq.util.binding;

import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;

public class QueryIteratorCount
    extends QueryIteratorWrapper
{
    protected long counter = 0;

    public QueryIteratorCount(QueryIterator qIter) {
        super(qIter);
    }

    /** The counter is incremented after each call to {@link #next()}. */
    public long getCounter() {
        return counter;
    }

    @Override
    protected Binding moveToNextBinding() {
        ++counter;
        return super.moveToNextBinding();
    }
}
