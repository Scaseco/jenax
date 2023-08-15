package org.aksw.jena_sparql_api.rdf.collections;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aksw.commons.collections.ConvertingList;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.AddDeniedException;
import org.apache.jena.util.iterator.NiceIterator;
import org.apache.jena.vocabulary.RDF;

import com.google.common.base.Converter;

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
    protected boolean isFwd;

    protected RDFList getList() {
        // Pick any resource and treat it as a list
        Resource o = ResourceUtils.getPropertyValue(s, p, isFwd, Resource.class);
        if(o == null) {
            o = RDF.nil.inModel(s.getModel());
        }

        // TODO Should we also clear any other value for consistency?
        // This would cause a get-method to cause a side effect - it breaks on read only models
        // // Also, clear all any other value for consistency
        if (false) {
            ensureNonNull();
        }

        return o.as(RDFList.class);
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
        ensureNonNull();
        return addAll(0, c);
        // return super.addAll(c);
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
        RDFList list = getList();
        RDFList parentCell = findParent(list, index);

        // If this list is empty then consume one item from the iterator and use it to initialize the list
        if (parentCell == null) {
            if (it.hasNext()) {
                RDFNode item = it.next();
                add(item);
                parentCell = getList();
            }
        }

        // Append the remaining items (if any)
        while (it.hasNext()) {
            RDFNode item = it.next();

            @SuppressWarnings("null")
            RDFList bak = parentCell.getTail();

            Resource newCellRaw = list.getModel().createResource(); //.as(RDFList.class);
            newCellRaw.addProperty(RDF.first, item);
            newCellRaw.addProperty(RDF.rest, bak);

            RDFList newCell = newCellRaw.as(RDFList.class);
            parentCell.setTail(newCell);
            parentCell = newCell;
        }

        return result;
    }

    @Override
    public void add(int index, RDFNode element) {
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

    public static RDFList getParent(RDFList child) {
        // TODO Somehow replace RDF.rest with the property referred to by the list implementation
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
//
//	public static shiftValue(RDFList parent) {
//		while(parent != null) {
//			RDFList child = parent.getTail();
//
//			if(parent.isEmpty()) {
//			} else {
//
//			}
//		}
//
//		if(list.isEmpty()) {
//			list.with(value)
//
//			Resource newElement = list.getModel().createResource();
//			newElement.addProperty(RDF.first, list.getHead());
//			newElement.addProperty(RDF.rest, list.getTail());
//
//			list.setTail(newElement);
//		} else {
//
//		}
//	}
//
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
        RDFList list = getList();
        Iterator<RDFNode> result = new RDFListIterator(s, p, list);

        return result;
    }
    //protected RDFList list;

    /**
     * <p>
     * Iterator that can step along chains of list pointers to the end of the
     * list.
     * </p>
     */
    public class RDFListIterator extends NiceIterator<RDFNode>
    {
        // Instance variables

        protected Resource s;
        protected Property p;

        /** The current list node */
        protected RDFList m_head;

        /** The most recently seen node */
        protected RDFList m_seen = null;


        // Constructor
        //////////////

        /**
         * Construct an iterator for walking the list starting at head
         */
        protected RDFListIterator(Resource s, Property p, RDFList head) {
            this.s = s;
            this.p = p;
            m_head = head;
        }


        // External contract methods
        ////////////////////////////

        /**
         * @see Iterator#hasNext
         */
        @Override public boolean hasNext() {
            return !m_head.isEmpty();
        }

        /**
         * @see Iterator#next
         */
        @Override public RDFNode next() {
            m_seen = m_head;
            m_head = m_head.getTail();

            return m_seen.getHead();
        }

        /**
         * @see Iterator#remove
         */
        @Override public void remove() {
            //RDFDataMgr.write(System.out, s.getModel(), RDFFormat.TURTLE_FLAT);


            if (m_seen == null) {
                throw new IllegalStateException( "Illegal remove from list operator" );
            }

            // If we modify the head, ensure the reference by (s, p) is updated.
//            RDFNode root = ResourceUtils.getPropertyValue(s, p);
//            if(Objects.equals(root, m_head)) {

            //RDFList element = m_seen.getTail();
            RDFList parent = getParent(m_seen);
            setTail(parent, m_head, s, p);

            //linkParentTo(element, s, p);
//        	RDFNode _parent = ResourceUtils.getReversePropertyValue(m_head, RDF.rest);
//        	m_head = m_head.getTail();
//        	if(_parent == null) {
//            	ResourceUtils.setProperty(s, p, m_head);
//        	} else {
//        		RDFList parent = _parent.as(RDFList.class);
//            	parent.setTail(m_head);
//        	}

            // will remove three statements in a well-formed list
            m_seen.removeProperties();
            //m_head = element;
            m_seen = null;
        }
    }

    // TODO hashCode and equals

    /** Return a (new) mutable view of the list as Nodes */
    public List<Node> asNodes() {
        Converter<RDFNode, Node> converter = Converter.from(RDFNode::asNode, s.getModel()::asRDFNode);
        return new ConvertingList<>(this, converter);
    }
}
