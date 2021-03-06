package org.aksw.jena_sparql_api.collection.observable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.List;
import java.util.Map;

import org.aksw.commons.collection.observable.ObservableValue;
import org.aksw.commons.collection.observable.Registration;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * A field that when setting its value removes the referred to triple
 * and replaces it with another one
 *
 * @author raven
 *
 * @param <T>
 */
public class RdfFieldFromExistingTriple
    implements ObservableValue<Node>
{
    protected GraphChange graph;
    protected Triple existingTriple;
    protected int componentIdx; // s p or o

    public RdfFieldFromExistingTriple(GraphChange graph, Triple existingTriple, int componentIdx) {
        super();
        this.graph = graph;
        this.existingTriple = existingTriple;
        this.componentIdx = componentIdx;
    }

    public Node getOriginalValue() {
        return TripleUtils.getNode(existingTriple, componentIdx);
    }


    public Node getLatestValue(Map<Triple, Triple> map) {
        Triple baseTriple = map.getOrDefault(existingTriple, existingTriple);
        List<Node> nodes = TripleUtils.tripleToList(baseTriple);
        Node result = nodes.get(componentIdx);
        return result;
    }


    @Override
    public Node get() {
        // return cachedValue;
        return getLatestValue(graph.getTripleReplacements());
    }


    @Override
    public void set(Node value) {
        // If the original triple was remapped than take that one as the base
        Triple baseTriple = graph.getTripleReplacements().getOrDefault(existingTriple, existingTriple);
        List<Node> nodes = TripleUtils.tripleToList(baseTriple);
        nodes.set(componentIdx, value);
        Triple newTriple = TripleUtils.listToTriple(nodes);

        graph.getTripleReplacements().put(existingTriple, newTriple);

//        pce.firePropertyChange("value", cachedValue, value);
    }

    protected PropertyChangeEvent adaptEvent(PropertyChangeEvent ev) {
        Map<Triple, Triple> oldMap = (Map<Triple, Triple>)ev.getOldValue();
        Map<Triple, Triple> newMap = (Map<Triple, Triple>)ev.getNewValue();

        Node oldValue = getLatestValue(oldMap);
        Node newValue = getLatestValue(newMap);

        return new PropertyChangeEvent(this, "value", oldValue, newValue);
    }

    @Override
    public Runnable addVetoableChangeListener(VetoableChangeListener listener) {
        return graph.getTripleReplacements().addVetoableChangeListener(ev -> {
            PropertyChangeEvent adapted = adaptEvent(ev);
            listener.vetoableChange(adapted);
        });
    }

    @Override
    public Registration addPropertyChangeListener(PropertyChangeListener listener) {
        return graph.getTripleReplacements().addPropertyChangeListener(ev -> {
            PropertyChangeEvent adapted = adaptEvent(ev);
            listener.propertyChange(adapted);
        });

//        return graph.addPostUpdateListener(ev -> {
//            Node oldNode = (Node)ev.getOldValue();
//            Node newNode = (Node)ev.getNewValue();
//            listener.propertyChange(new PropertyChangeEvent(this, "value", oldNode, newNode));
//            // TODO Trap potential endless loop by introducing a flag when we change the value
//            // in response to an event
//            set(getLatestValue());
//        });

//        pce.addPropertyChangeListener(listener);
//        return () -> pce.removePropertyChangeListener(listener);
    }

}