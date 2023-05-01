package org.aksw.jena_sparql_api.sparql.ext.distinct;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.DistinctDataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.InternalErrorException;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.engine.binding.BindingProjectNamed;
import org.apache.jena.sparql.engine.iterator.QueryIter1;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.system.SerializationFactoryFinder;

import com.google.common.base.Preconditions;

/**
 * A variant of QueryIterDistinct where a list of conditions can be specified when to apply distinct.
 * If a binding does not satisfy any condition is returned (streaming) without causing resource overhead
 * of tracking it.
 * Every condition has its own set of seen bindings. As soon as one condition evaluates to true, the
 * binding is added to that condition's bucket. Further conditions are then not evaluated further
 * (short circuit evaluation).
 *
 */
public class QueryIterDistinctConditional extends QueryIter1
{
    private long memThreshold = Long.MAX_VALUE ;    // Default "off" value.
    private Binding slot = null ;
    private final List<SortCondition> preserveOrder;
    private Iterator<Binding> iterator = null ;

    /** Buckets for conditionally applying distinct based on expressions */
    private Collection<Bucket> buckets;

    protected class Bucket {
        protected ExprList exprs;
        protected DistinctDataBag<Binding> db = null ;
        protected Set<Binding> seen = new LinkedHashSet<>() ;

        public Bucket(ExprList exprs) {
            super();
            this.exprs = exprs;
        }

        public boolean isSpilling() {
            return db != null;
        }

        public void startSpilling() {
            Preconditions.checkState(!isSpilling(), "Bucket already in spilling mode");

            ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(getExecContext().getContext()) ;
            Comparator<Binding> comparator = new BindingComparator(preserveOrder, getExecContext()) ;
            this.db = BagFactory.newDistinctBag(policy, SerializationFactoryFinder.bindingSerializationFactory(), comparator) ;
        }

        public void add(Binding b) {
            if (isSpilling()) {
                db.add(b);
            } else {
                seen.add(b);
            }
        }

        public boolean containsSeen(Binding b) {
            return seen.contains(b);
        }

        public void close() {
            if (db != null) {
                db.close();
            }
            db = null;
            seen = null;
        }
    }

    public QueryIterDistinctConditional(QueryIterator qIter, List<SortCondition> preserveOrder, ExecutionContext execCxt, Collection<ExprList> conditions) {
        super(qIter, execCxt) ;
        this.preserveOrder = (preserveOrder!=null) ? preserveOrder : Collections.emptyList();
        if ( execCxt != null ) {
            memThreshold = execCxt.getContext().getLong(ARQ.spillToDiskThreshold, memThreshold) ;
            if ( memThreshold < 0 )
                throw new ARQException("Bad spillToDiskThreshold: "+memThreshold) ;
        }

        this.buckets = conditions.stream().map(Bucket::new).collect(Collectors.toList());
    }

    public Bucket getBucket(Binding b) {
        // At this point, we are currently in the initial pre-threshold mode.
        Bucket result = null;
        for (Bucket bucket : buckets) {
            boolean isConditionMet = bucket.exprs.isSatisfied(b, getExecContext());
            if (isConditionMet) {
                result = bucket;
                break;
            }
        }
        return result;
    }

    @Override
    protected boolean hasNextBinding() {
        if (slot != null)
            return true;
        if ( iterator != null )
            // Databag active.
            return iterator.hasNext() ;

        slot = null;
        while (getInput().hasNext()) {
            Binding b = getInputNext();
            Bucket bucket = getBucket(b);
            if (bucket != null) {
                if (bucket.isSpilling()) {
                    bucket.add(b);
                } else {
                    if (bucket.containsSeen(b)) {
                        continue;
                    } else if (bucket.seen.size() < memThreshold) {
                        bucket.add(b);
                        slot = b;
                        break;
                    } else if (!bucket.isSpilling()) {
                        bucket.startSpilling();
                        bucket.add(b);
                    }
                }
            } else {
                slot = b;
                break;
            }
        }

        boolean result;
        if (slot != null) {
            result = true;
        } else {
            // All data exhausted - set up an iterator over databags
            iterator = Iter.iter(buckets).flatMap(b ->
                b.db == null ? Iter.empty() : Iter.onClose(b.db.iterator(), () -> b.close()));
            result = iterator.hasNext();
        }

        // Leave slot null.
        return result;
    }

    /** Return the binding from the input, hiding any variables to be ignored. */
    private Binding getInputNext() {
        Binding b = getInput().next() ;
        // Hide unnamed and internal variables.
        b = new BindingProjectNamed(b) ;
        return b ;
    }

    @Override
    protected Binding moveToNextBinding() {
        if ( slot != null ) {
            Binding b = slot ;
            slot = null ;
            return b ;
        }
        if ( iterator != null ) {
            Binding b = iterator.next() ;
            return b ;
        }
        throw new InternalErrorException() ;
    }

    @Override
    protected void closeSubIterator() {
        if ( iterator != null ) {
            iterator = null ;
            Iter.close(iterator);
        }
        buckets = null;
    }

    // We don't need to do anything. We're a QueryIter1
    // and that handles the cancellation of the wrapped
    // iterator.
    @Override
    protected void requestSubCancel()
    { }

}