package org.aksw.jenax.arq.service.vfs;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.DataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIteratorWrapper;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.Context;

/**
 * A QueryIterator that upon access to the first item consumes the underlying
 * iterator into a data bag.
 */
// XXX This class also exists in service enhancer - reuse from there once PR is through.
public class QueryIteratorMaterialize extends QueryIteratorWrapper {
    protected QueryIterator outputIt = null;
    protected ExecutionContext execCxt;

    /** If the threshold policy is not set then it will be lazily initialized from the execCxt */
    protected ThresholdPolicy<Binding> thresholdPolicy;

    public QueryIteratorMaterialize(QueryIterator qIter, ExecutionContext execCxt) {
        this(qIter, execCxt, null);
    }

    /** Ctor with a fixed threshold policy. */
    public QueryIteratorMaterialize(QueryIterator qIter, ExecutionContext execCxt, ThresholdPolicy<Binding> thresholdPolicy) {
        super(qIter);
        this.execCxt = execCxt;
        this.thresholdPolicy = thresholdPolicy;
    }

    /**
     * Get the threshold policy.
     * May return null if it was not initialized yet.
     * Call {@link #hasNext()} to force initialization.
     */
    public ThresholdPolicy<Binding> getThresholdPolicy() {
        return thresholdPolicy;
    }

    @Override
    protected boolean hasNextBinding() {
        collect();
        return outputIt.hasNext();
    }

    @Override
    protected Binding moveToNextBinding() {
        collect();
        Binding b = outputIt.next();
        return b;
    }

    protected void collect() {
        if (outputIt == null) {
            Context cxt = execCxt == null ? null : execCxt.getContext();
            if (thresholdPolicy == null) {
                thresholdPolicy = ThresholdPolicyFactory.policyFromContext(cxt);
            }
            DataBag<Binding> db = BagFactory.newDefaultBag(thresholdPolicy, SerializationFactoryFinder.bindingSerializationFactory());
            try {
                db.addAll(iterator);
            } finally {
                iterator.close();
            }
            outputIt = QueryIterPlainWrapper.create(db.iterator(), execCxt);
        }
    }

    @Override
    protected void closeIterator() {
        // If the output iterator is set, then the input iterator has been consumed and closed.
        if (outputIt != null) {
            outputIt.close();
        } else {
            // Output iterator was not created -> close the input iterator.
            super.closeIterator();
        }
    }
}
