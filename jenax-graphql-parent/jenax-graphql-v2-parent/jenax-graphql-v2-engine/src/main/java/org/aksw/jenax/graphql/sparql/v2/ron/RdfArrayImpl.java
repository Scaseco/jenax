package org.aksw.jenax.graphql.sparql.v2.ron;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

// In principle RdfArray could extend RdfObject - similar to JavaScript - but probably sticking to
// the JSON model is easier to use.
public class RdfArrayImpl
    // extends AbstractList<RdfElement>
    //implements RdfElement,
    extends RdfElementNodeBase
    implements RdfArray
{
    // Maybe elements should be restricted to RdfObject
    protected List<RdfElement> elements;

    public RdfArrayImpl() {
        this(NodeFactory.createBlankNode(), new ArrayList<>());
    }

    protected RdfArrayImpl(Node node, List<RdfElement> elements) {
        super(node);
        this.elements = elements;
    }

    @Override
    public <T> T accept(RdfElementVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public RdfArray add(RdfElement element) {
        int i = elements.size();
        elements.add(element);

        ((RdfElementNodeBase)element).setParent(new ParentLinkArrayImpl(this, i));

        return this;
    }

    @Override
    public int size() {
        return elements.size();
    }

    // @Override
    public RdfArray addAll(RdfArray other) {
        other.forEach(elements::add);
        // elements.addAll(other);
        return this;
    }

    // @Override
    public RdfArray addAll(Collection<? extends RdfElement> c) {
        elements.addAll(c);
        return this;
    }

    @Override
    public RdfElement get(int index) {
        return elements.get(index);
    }

    @Override
    public Iterator<RdfElement> iterator() {
        return elements.iterator();
    }

    @Override
    public RdfArray set(int index, RdfElement element) {
        RdfElement elt = elements.get(index);
        if (elt != null) {
            RdfElementNodeBase x = (RdfElementNodeBase)elt;
            x.setParent(null);
            ((RdfElementNodeBase)element).setParent(new ParentLinkArrayImpl(this, index));
        }

        return this;
    }

    @Override
    public RdfArray remove(int index) {
        elements.remove(index);
        Iterator<RdfElement> it = elements.listIterator(index);

        // Decrement parent link index of all elements after the removed one
        while (it.hasNext()) {
            RdfElementNodeBase elt = (RdfElementNodeBase)it.next();
            ParentLinkArray oldLink = elt.getParent().asArrayLink();
            elt.setParent(new ParentLinkArrayImpl(oldLink.getParent(), oldLink.getIndex() - 1));
        }
        return this;
    }
}
