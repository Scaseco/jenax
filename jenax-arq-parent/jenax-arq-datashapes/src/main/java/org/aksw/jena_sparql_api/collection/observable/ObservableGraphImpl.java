package org.aksw.jena_sparql_api.collection.observable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.collection.observable.CollectionChangedEventImpl;
import org.aksw.commons.collection.observable.Registration;
import org.aksw.jena_sparql_api.rx.GraphFactoryEx;
import org.aksw.jenax.arq.util.triple.SetFromGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.Difference;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.GraphWrapper;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

import com.google.common.collect.Sets;


/**
 * A graph wrapper that overrides the {@link #add(Triple)} and {@link #delete(Triple)} methods
 * such that duplicate insertions and removals are suppressed and thus do not fire
 * superfluous events.
 *
 * More importantly, the {@link #addPropertyChangeListener(PropertyChangeListener)} method is provided
 * which fires events <b>BEFORE</b> changes occur on the graph. Hence, the old state of the graph
 * is accessible during event processing.
 * The raised events are instances of {@link CollectionChangedEventImpl} which is a subclass of
 * {@link PropertyChangeEvent}.
 *
 * Note that {@link #getEventManager()} fires events <b>AFTER</b> changes already occurred.
 *
 * @author raven
 *
 */
public class ObservableGraphImpl
    extends GraphWrapper
    implements ObservableGraph
{
    /** Whether to see if a quad action will change the dataset - test before add for existence, test before delete for absence */
    protected boolean CheckFirst = true ;
    /** Whether to record a no-op (maybe as a comment) */
    protected boolean RecordNoAction = true ;

    protected VetoableChangeSupport vcs = new VetoableChangeSupport(this);
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public static ObservableGraphImpl decorate(Graph delegate) {
        return new ObservableGraphImpl(delegate);
    }

    public ObservableGraphImpl(Graph delegate)
    {
        super(delegate) ;
    }

    @Override
    public boolean delta(Collection<? extends Triple> rawAdditions, Collection<?> rawDeletions) {
        return applyDeltaGraph(
            // Wrap as a non-observable set in order to not fire events
            // prematurely as this.asSet() would do!
            this, get(),
            vcs, pcs,
            false,
            rawAdditions, rawDeletions);
    }



    public static boolean applyDeltaGraph(
            Graph self,
            Graph backend,
            VetoableChangeSupport vcs,
            PropertyChangeSupport pcs,
            boolean clearIntersection,
            Collection<? extends Triple> rawAdditions, Collection<?> rawRemovals) {

        Set<Triple> backendAsSet = SetFromGraph.wrap(backend);

        // Set up the physical removals / additions that will be sent to the backend
        // This may include overlapping items
        Set<Triple> physRemovals = rawRemovals == self
            ? rawRemovals.stream().map(x -> (Triple)x).collect(Collectors.toCollection(LinkedHashSet::new))
            : rawRemovals.stream().filter(backendAsSet::contains).map(x -> (Triple)x).collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Triple> physAdditions = rawAdditions.stream()
                .filter(x -> !backend.contains(x) || physRemovals.contains(x))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Triple> intersection = new LinkedHashSet<>(Sets.intersection(physAdditions, physRemovals));

        Set<Triple> as;
        Set<Triple> rs;

        if (clearIntersection || intersection.isEmpty()) {
            physRemovals.removeAll(intersection);
            physAdditions.removeAll(intersection);
            as = physAdditions;
            rs = physRemovals;
        } else {
            // Set up the change sets
            as = new LinkedHashSet<>(physAdditions);
            rs = new LinkedHashSet<>(physRemovals);

            as.removeAll(intersection);
            rs.removeAll(intersection);
        }

        Graph gas = GraphFactory.createDefaultGraph();
        Graph grs = GraphFactory.createDefaultGraph();

        GraphUtil.add(gas, as.iterator());
        GraphUtil.add(grs, rs.iterator());

        // FIXME additions and removals may have common items! those should be removed in
        // the event's additions / removals sets

        boolean result = false;

        {
            Graph oldValue = self;
            Graph newValue = rawRemovals == self
                    ? gas
                    : new Union(new Difference(backend, grs), gas);

            try {
                vcs.fireVetoableChange(new CollectionChangedEventImpl<>(
                        self, oldValue, newValue,
                        as, rs, Collections.emptySet()));
            } catch (PropertyVetoException e) {
                throw new RuntimeException(e);
            }
        }

        boolean changeByRemoval;
        if (rawRemovals == self) {
            changeByRemoval = !backend.isEmpty();
            if (changeByRemoval) {
                // Only invoke clear if we have to; prevent triggering anything
                backend.clear();
            }
        } else {
            changeByRemoval = backendAsSet.removeAll(physRemovals);
        }

        boolean changeByAddition = backendAsSet.addAll(physAdditions);
        result = changeByRemoval || changeByAddition;

        {
            Graph oldValue = rawRemovals == self
                    ? grs
                    : new Union(new Difference(backend, gas), grs);
            Graph newValue = self;

            pcs.firePropertyChange(new CollectionChangedEventImpl<>(
                    self, oldValue, newValue,
                    as, rs, Collections.emptySet()));
        }

        return result;
    }


//
//    public void postponeEvents(boolean onOrOff) {
//
//    }
//
//    public void firePostponedEvents() {
//
//    }
//

    @Override public void add(Triple quad)
    {
        if ( CheckFirst && contains(quad) )
        {
//            if ( RecordNoAction )
//                recordVetoable(QuadAction.NO_ADD, quad) ;
            return ;
        }
        add$(quad) ;
    }

    private void add$(Triple quad)
    {
        recordVetoable(QuadAction.ADD, quad) ;
        super.add(quad) ;
        record(QuadAction.ADD, quad);
    }

    @Override public void delete(Triple quad)
    {
        if ( CheckFirst && ! contains(quad) )
        {
//            if ( RecordNoAction )
//                recordVetoable(QuadAction.NO_DELETE, quad) ;
            return ;
        }
        delete$(quad) ;
    }

    private void delete$(Triple quad)
    {
        recordVetoable(QuadAction.DELETE, quad) ;
        super.delete(quad) ;
        record(QuadAction.DELETE, quad);
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        deleteAny(this, Triple.createMatch(s, p, o), pcs);
    }

//    @Override
//    public void clear() {
//        deleteAny(this, Triple.createMatch(null, null, null), pcs);
//    }

    private static int SLICE = 1000 ;

    // @Override
    public static void deleteAny(
            Graph graph,
            Triple pattern,
            PropertyChangeSupport pcs
            )
    {
        int n;
        do {
            Iterator<Triple> iter = graph.find(pattern) ;

            Graph deletions = GraphFactoryEx.createInsertOrderPreservingGraph();

            for (n = 0; n < SLICE & iter.hasNext(); ++n) {
                Triple t = iter.next();
                deletions.add(t);
            }

            pcs.firePropertyChange(new CollectionChangedEventImpl<Triple>(graph,
                    graph, new Difference(graph, deletions),
                    Collections.emptySet(), new SetFromGraph(deletions), null));
        } while (n >= SLICE);
    }

    private void recordVetoable(QuadAction action, Triple t)
    {
        Set<Triple> additions;
        Set<Triple> deletions;

        Graph tmp;
        switch (action) {
        case ADD:
            additions = Collections.singleton(t);
            deletions = Collections.emptySet();

            tmp = GraphFactory.createDefaultGraph();
            tmp.add(t);

            try {
                vcs.fireVetoableChange(new CollectionChangedEventImpl<Triple>(this,
                        this, new Union(this, tmp),
                        additions, deletions, null));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            break;
        case DELETE:
            additions = Collections.emptySet();
            deletions = Collections.singleton(t);

            tmp = GraphFactory.createDefaultGraph();
            tmp.add(t);

            try {
                vcs.fireVetoableChange(new CollectionChangedEventImpl<Triple>(this,
                        this, new Difference(this, tmp), additions, deletions, null));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            break;
        default:
            // nothing to do
            break;
        }
    }


    private void record(QuadAction action, Triple t)
    {
        Set<Triple> additions;
        Set<Triple> deletions;

        Graph tmp;
        switch (action) {
        case ADD:
            additions = Collections.singleton(t);
            deletions = Collections.emptySet();

            tmp = GraphFactory.createDefaultGraph();
            tmp.add(t);

            pcs.firePropertyChange(new CollectionChangedEventImpl<Triple>(this,
                    new Difference(this, tmp), this,
                    additions, deletions, Collections.emptySet()));
            break;
        case DELETE:
            additions = Collections.emptySet();
            deletions = Collections.singleton(t);

            tmp = GraphFactory.createDefaultGraph();
            tmp.add(t);

            pcs.firePropertyChange(new CollectionChangedEventImpl<Triple>(this,
                    new Union(this, tmp), this, additions, deletions, Collections.emptySet()));
            break;
        default:
            // nothing to do
            break;
        }
    }
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        vcs.addVetoableChangeListener(listener);
        return () -> vcs.removeVetoableChangeListener(listener);
    }

    public Registration addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
        // return () -> pcs.removePropertyChangeListener(listener);
        return Registration.from(
            () -> {
                listener.propertyChange(new CollectionChangedEventImpl<Triple>(this,
                        this, this,
                        Collections.emptySet(), Collections.emptySet(), Collections.emptySet()));
            },
            () -> pcs.removePropertyChangeListener(listener));
    }

    //public static <T> ExtendedIterator<T> wrapWithClose()

    @Override
    public ExtendedIterator<Triple> find(Node s, Node p, Node o) {
        // Wrap iterator such that the remove method fires deletion events
        ExtendedIterator<Triple> it = super.find(s, p, o);

        return WrappedIterator.create(new SinglePrefetchClosableIterator<Triple>() {
            @Override
            protected Triple prefetch() throws Exception {
                Triple result = it.hasNext() ? it.next() : finish();
                return result;
            }

            @Override
            protected void doRemove(Triple item) {
                recordVetoable(QuadAction.DELETE, item);
                it.remove();
                record(QuadAction.DELETE, item);
            }

            @Override
            public void close() {
                it.close();
            }
        });
    }

//    @Override
//    public void sync() {
//        SystemARQ.syncObject(monitor) ;
//        super.sync() ;
//    }

//    @Override
//    public Graph getDefaultGraph() {
//        return createDefaultGraph(this);
//    }
//
//    @Override
//    public Graph getGraph(Node graphNode) {
//        return createNamedGraph(this, graphNode);
//    }
}
