package org.aksw.jena_sparql_api.collection.observable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collection.observable.CollectionChangedEvent;
import org.aksw.commons.collection.observable.CollectionChangedEventImpl;
import org.aksw.commons.collection.observable.ForwardingDeltaCollectionBase;
import org.aksw.commons.collection.observable.ObservableMap;
import org.aksw.commons.collection.observable.ObservableMapImpl;
import org.aksw.commons.collection.observable.ObservableSet;
import org.aksw.commons.collection.observable.ObservableSets;
import org.aksw.commons.collection.observable.Registration;
import org.aksw.jenax.arq.util.triple.SetFromGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


/**
 * Set view over the values of a property of a given subject resource.
 *
 * Issue: Jena's event mechanism does not seem to allow getting actual graph changes; i.e. ignoring
 * events for redundant additions or deletions.
 * Also, there does not seem to be an integration with transaction - i.e. aborting a transaction
 * should raise an event that undos all previously raised additions/deletions.
 *
 * @author raven Nov 25, 2020
 *
 * @param <T>
 */
public class ObservableSetFromGraph
    //extends SetFromGraph
    extends ForwardingDeltaCollectionBase<Triple, Set<Triple>>
    implements ObservableSet<Triple>
//    implements RdfBackedCollection<Node>
{
    protected ObservableGraph graph;

    public ObservableSetFromGraph(ObservableGraph graph) {
        super(SetFromGraph.wrap(graph));
        this.graph = graph;
    }

    @Override
    public boolean delta(Collection<? extends Triple> additions, Collection<?> removals) {
        return getGraph().delta(additions, removals);
    }


    public ObservableGraph getGraph() {
        return graph;
    }

//    @Override
//    public ObservableGraph getGraph() {
//        return graph;
//        // return (ObservableGraph)((SetFromGraph)super.getGraph();
//    }

//    @Override
//    public boolean add(Triple t) {
////        Triple t = createTriple(node);
//
//        boolean result = !graph.contains(t);
//
//        if (result) {
//            graph.add(t);
//        }
//        return result;
//    }
//
    protected PropertyChangeEvent convertEvent(PropertyChangeEvent ev) {
        CollectionChangedEvent<Triple> oldEvent = (CollectionChangedEvent<Triple>)ev;

        return new CollectionChangedEventImpl<Triple>(
            this,
            this,
            // (SetFromGraph)oldEvent.getNewValue(), //
            SetFromGraph.wrap((Graph)oldEvent.getNewValue()),
            oldEvent.getAdditions(),
            oldEvent.getDeletions(),
            oldEvent.getRefreshes()
        );
    }


    /**
    *
    * @return A Runnable that de-registers the listener upon calling .run()
    */
   @Override
   public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
       return getGraph().addVetoableChangeListener(ev -> {
           PropertyChangeEvent newEvent = convertEvent(ev);
           listener.vetoableChange(newEvent);
       });
   }

    /**
     *
     * @return A Runnable that de-registers the listener upon calling .run()
     */
    @Override
    public Registration addPropertyChangeListener(PropertyChangeListener listener) {
        return getGraph().addPropertyChangeListener(ev -> {
            PropertyChangeEvent newEvent = convertEvent(ev);
            listener.propertyChange(newEvent);
        });
    }


    public static ObservableSetFromGraph decorate(Graph graph) {
        ObservableGraph tmp = ObservableGraphImpl.decorate(graph);
        ObservableSetFromGraph result = new ObservableSetFromGraph(tmp);
        return result;
    }


    public static void main(String[] args) {
        if (false) {
            ObservableMap<String, String> map = ObservableMapImpl.decorate(new LinkedHashMap<>());
            ObservableSet<String> set = map.keySet();

            set.addPropertyChangeListener(ev -> System.out.println("KeySet changed: " + ev));

            map.addPropertyChangeListener(event -> {
                CollectionChangedEvent<Entry<String, String>> ev = (CollectionChangedEvent<Entry<String, String>>)event;
                System.out.println("Change:");
                System.out.println("  Old Value:" + ev.getOldValue());
                System.out.println("  New Value:" + ev.getNewValue());
                System.out.println("  Added: " + ev.getAdditions() + " Removed: " + ev.getDeletions());
            });


            map.put("a", "hello");
            map.put("b", "world");
            map.put("a", "hey");
            map.clear();
        }

        if (true) {
            ObservableSet<Triple> a = ObservableSetFromGraph.decorate(GraphFactory.createPlainGraph());
            ObservableMap<Triple, Triple> map =  ObservableMapImpl.decorate(new LinkedHashMap<Triple, Triple>());
            ObservableSet<Triple> b = map.keySet();

            ObservableSet<Triple> effectiveTriples = ObservableSets.union(a, b);
            effectiveTriples.addPropertyChangeListener(ev -> System.out.println(ev));

            Triple t1 = Triple.create(RDF.Nodes.type, RDF.Nodes.type, RDF.Nodes.type);
            Triple t2 = Triple.create(RDFS.Nodes.label, RDFS.Nodes.label, RDFS.Nodes.label);

            a.add(t1);
            map.put(t2, t2);
            map.remove(t2);
        }

    }
}
