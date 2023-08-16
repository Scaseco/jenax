package org.aksw.jena_sparql_api.rdf.collections;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.aksw.commons.collections.ConvertingList;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.vocabulary.RDF;

import com.google.common.base.Converter;
import com.google.common.base.Preconditions;

/**
 * Create a modifiable {@link List} view over an {@link RDFList}
 *
 *
 * @author Claus Stadler, Jan 18, 2019
 *
 */
public class ListFromRDFList
    extends AbstractList<RDFNode>
{
    protected Resource s;
    protected Property p;
    protected boolean isFwd = true;

    public static RDFList getList(Resource s, Property p) {
        // Pick any resource and treat it as a list
        Resource o = ResourceUtils.getPropertyValue(s, p, true, Resource.class);
        if(o == null) {
            o = RDF.nil.inModel(s.getModel());
        }

        // TODO Should we also clear any other value for consistency?
        // This would cause a get-method to cause a side effect - it breaks on read only models
        // // Also, clear all any other value for consistency
        return o.as(RDFList.class);
    }

    protected RDFList getList() {
        RDFList result = getList(s, p);
        return result;
    }

    /** Ensure that the value for property p of source node s is non null. If it is then set that value to rdf:nil. */
    public ListFromRDFList ensureNonNull() {
        Resource o = ResourceUtils.getPropertyValue(s, p, isFwd, Resource.class);
        if(o == null) {
            o = RDF.nil.inModel(s.getModel());

            try {
                ResourceUtils.setProperty(s, p, isFwd, o);
            } catch (AddDeniedException e) {
            }
        }
        return this;
    }

    /** Ensure that adding an empty collection to a non-intialized list result in termination with rdf:nil */
    @Override
    public boolean addAll(Collection<? extends RDFNode> c) {
        // ensureNonNull();
        return addAll(0, c);
    }

    public ListFromRDFList(Resource subject, Property property) {
        this(subject, property, true);
    }

    public static ListFromRDFList create(Resource subject, Property property) {
        return new ListFromRDFList(subject, property);
    }

    public ListFromRDFList(Resource subject, Property property, boolean isFwd) {
        super();
        this.s = subject;
        this.p = property;
        this.isFwd = isFwd;
    }

    @Override
    public boolean contains(Object o) {
        boolean result = o instanceof RDFNode
                ? getList().contains((RDFNode)o)
                : false;
        return result;
    }

    @Override
    public boolean add(RDFNode e) {
        // Use of listIterator() avoided because it would require computing size() first
        RDFList newList = getList().with(e);
        ResourceUtils.setProperty(s, p, newList);
        //System.out.println("list prop: " + s.getProperty(p));
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends RDFNode> c) {
        return addAll(index, c.iterator());
    }

    public boolean addAll(int index, Iterator<? extends RDFNode> it) {
        boolean result = it.hasNext();
        ListIterator<RDFNode> listIt = listIterator(index);
        while (it.hasNext()) {
            RDFNode item = it.next();
            listIt.add(item);
        }
        return result;
    }

    @Override
    public void add(int index, RDFNode element) {
        boolean useListIterator = true;
        if (useListIterator) {
            listIterator(index).add(element);
        } else {
            RDFList list = getList();
            RDFList parentCell = findParent(list, index);

            if(parentCell == null) {
                RDFList newCell = list.cons(element);
                ResourceUtils.setProperty(s, p, newCell);
            } else {
                RDFList remainder = parentCell.cons(element);
                remainder.setTail(parentCell.getTail());
                parentCell.setTail(remainder);
            }
        }
    }

    public static RDFList getParent(RDFList child) {
        Objects.requireNonNull(child);
        // Ideally somehow replace RDF.rest with the property referred to by the list implementation
        RDFNode _parent = ResourceUtils.getReversePropertyValue(child, RDF.rest);
        RDFList result = _parent == null ? null : _parent.as(RDFList.class);
        return result;
    }

    public static void setTail(RDFList parent, RDFList element, Resource s, Property p) {
        if(parent == null) {
            ResourceUtils.setProperty(s, p, element);
        } else {
            parent.setTail(element);
        }
    }

    public static void linkParentTo(RDFList element, Resource s, Property p) {
        RDFList parent = getParent(element);
        setTail(parent, element, s, p);
    }

    @Override
    public RDFNode get(int index) {
        RDFList list = getList();
        RDFNode result = list.get(index);
        return result;
    }

    @Override
    public RDFNode set(int index, RDFNode element) {
        RDFList list = getList();
        RDFList item = findElement(list, index);

        item.setHead(element);

        return element;
    }

    public static RDFList findParent(RDFList list, int index) {
        RDFList result;
        if(index == 0) {
            result = null;
        } else {
            result = list;
            int n = index - 1;
            for(int i = 0; i < n; ++i) {
                result = result.getTail();
            }
        }

        return result;
    }

    public static RDFList findElement(RDFList list, int index) {
        RDFList result = list;
        for(int i = 0; i < index; ++i) {
            result = result.getTail();
        }

        return result;
    }

    @Override
    public int size() {
        RDFList list = getList();
        int result = list.size();
        return result;
    }

    @Override
    public Iterator<RDFNode> iterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<RDFNode> listIterator(int index) {
        RDFList list = getList();
        RDFList parentCell = findParent(list, index);
        ListIterator<RDFNode> result = new RDFListIterator(parentCell, index);
        return result;
    }

    /**
     * <p>
     * Iterator that can step along chains of list pointers to the end of the
     * list.
     * </p>
     */
    public class RDFListIterator
        implements ListIterator<RDFNode>
    {
//        protected Resource s;
//        protected Property p;

        /** The current list node */
        protected RDFList cursorCell; // null -> start, rdf:nil -> end
        protected boolean reachedEnd = false;

        /** The most recently seen node */
        protected RDFList seenCell = null;

        protected int currentIndex;

        /**
         * Construct an iterator for walking the list starting at head
         */
        protected RDFListIterator(RDFList head, int currentIndex) {
            this.cursorCell = head;
            this.currentIndex = currentIndex;
        }

        @Override
        public boolean hasNext() {
            RDFList nextCell = getNextCell(cursorCell);
            return !nextCell.isEmpty();
        }

        protected RDFList getNextCell(RDFList cell) {
            RDFList result;
            if (cell == null) {
                result = getList();
            } else if (cell.isEmpty()) {
                result = cell;
            } else {
                result = cell.getTail();
            }
            return result;
        }

        @Override
        public RDFNode next() {
            // Cursor always points to the parent of the current cell
            RDFList currentCell = getNextCell(cursorCell);
            seenCell = currentCell; // currentCell.isEmpty() ? null : currentCell;
            if (currentCell.isEmpty()) {
                Preconditions.checkState(!reachedEnd, "Already at end");
                reachedEnd = true;
            } else {
                cursorCell = currentCell;
                reachedEnd = false;
                ++currentIndex;
            }
            RDFNode result = currentCell.getHead();
            return result;
        }

        @Override
        public void remove() {
            // RDFDataMgr.write(System.out, s.getModel(), RDFFormat.TURTLE_FLAT);
            Preconditions.checkState(seenCell != null, "Illegal remove from list operator");

            cursorCell = cursorCell == null ? null : getParent(cursorCell);

            RDFList parent = getParent(seenCell);
            setTail(parent, seenCell.getTail(), s, p);

            // will remove three statements in a well-formed list
            seenCell.removeProperties();
            seenCell = null;
        }

        protected RDFList getPreviousCell() {
            RDFList result = cursorCell == null
                    ? null
                    : reachedEnd
                        ? cursorCell
                        : getParent(cursorCell);
            return result;
        }
        @Override
        public boolean hasPrevious() {
            return cursorCell != null;
        }

        @Override
        public RDFNode previous() {
            Preconditions.checkState(cursorCell != null, "Already at beginning of list");
            seenCell = cursorCell;
            RDFNode result = cursorCell.getHead();
            cursorCell = getPreviousCell();
            --currentIndex;
            reachedEnd = false;
            return result;
        }

        @Override
        public int nextIndex() {
            return hasNext() ? currentIndex + 1 : currentIndex;
        }

        @Override
        public int previousIndex() {
            return hasPrevious() ? currentIndex - 1 : currentIndex;
        }

        @Override
        public void set(RDFNode e) {
            Preconditions.checkState(seenCell != null && !reachedEnd);
            seenCell.setHead(e);
        }

        @Override
        public void add(RDFNode e) {
            RDFList newCell;
            // add(9):
            // 1 2 3 4
            //     ^ returned by next()
            // 1 2 9 3 4
            //       ^ still returned by next()
            if(cursorCell == null) {
                // Insert before first element
                RDFList list = getList(s, p);
                newCell = list.cons(e);
                ResourceUtils.setProperty(s, p, newCell);
                cursorCell = newCell;
                reachedEnd = cursorCell.getTail().equals(RDF.nil);
            } else if (reachedEnd) {
                // Insert after m_head
                newCell = newListCell(e, cursorCell.getTail());
                cursorCell.setTail(newCell);
                cursorCell = newCell;
            } else {
                RDFList next = getNextCell(cursorCell);
                newCell = newListCell(e, next);
                setTail(cursorCell, newCell, s, p);
                cursorCell = newCell;
            }
            seenCell = newCell;
            ++currentIndex;
        }
    }

    public RDFList newListCell(RDFNode value, Resource tail) {
        Resource cell = s.getModel().createResource();
        cell.addProperty(RDF.first, value);
        cell.addProperty(RDF.rest, tail);
        return cell.as(RDFList.class);
    }

    // Is hashCode and equals from the base class sufficient?

    /** Return a (new) mutable view of the list as Nodes */
    public List<Node> asNodes() {
        Converter<RDFNode, Node> converter = Converter.from(RDFNode::asNode, s.getModel()::asRDFNode);
        return new ConvertingList<>(this, converter);
    }
}
