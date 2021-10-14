package org.aksw.jenax.constraint.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.constraint.api.Contradictable;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.ValueSpaceClassification;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * Track ranges of nodes in their respective value space classifications (vsc).
 *
 * An empty set of ranges is treated as 'contradiction'. This means it is not possible
 *
 * FIXME Use of non-singletons in the 'unknown' value space must be handled as effectively
 * unconstrained
 *
 *      | zzz    999
 *      |
 *      | ...    ..
 *      | a      1
 *      |______________ -> value spaces
 *        |       |
 *      string   int   ...
 *
 * @author raven
 *
 */
public class NodeRanges
    implements Contradictable, Cloneable
{
    // Additional (pseudo) value space classifications for unform handing of IRIs and bnodes
    public static final String VSC_IRI = "xVSPACE_IRI";
    public static final String VSC_BNODE = "xVSPACE_BNODE";
    public static final String VSC_TRIPLE = "xVSPACE_TRIPLE";

    /**
     * A value of null means unconstrained.
     * Object in order to allow for future custom value spaces.
     */
    protected Map<Object, RangeSet<NodeWrapper>> vscToRangeSets = new HashMap<>();


    /** If true then only the value spaces in vsc can exist. Otherwise there may exist other value spaces. */
    protected boolean isVscExhaustive;


    protected NodeRanges(boolean isVscExhaustive) {
        super();
        this.isVscExhaustive = isVscExhaustive;
    }

    public RangeSet<NodeWrapper> getIriRanges() {
        return vscToRangeSets == null
                ? ImmutableRangeSet.of()
                : vscToRangeSets.getOrDefault(VSC_IRI, ImmutableRangeSet.of());
    }


    public NodeRanges(Map<Object, RangeSet<NodeWrapper>> vscToRangeSets) {
        super();
        this.vscToRangeSets = vscToRangeSets;
    }


    /**
     * Create an independent copy of this object
     */
    @Override
    public NodeRanges clone() {
        Map<Object, RangeSet<NodeWrapper>> clone = vscToRangeSets.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> TreeRangeSet.create(e.getValue())));
        return new NodeRanges(clone);
    }

    /** Create a NodeRange that contains everything */
    public static NodeRanges createOpen() {
        return new NodeRanges(false);
    }

    /** Create a NodeRange that contains nothing */
    public static NodeRanges createClosed() {
        return new NodeRanges(true);
    }

    /** Unconstrained mode means that any valid srange is considered enclosed by this one */
    public boolean isUnconstrained() {
        return !isVscExhaustive && vscToRangeSets.isEmpty();
    }

    @Override
    public boolean isConflicting() {
        return isVscExhaustive && vscToRangeSets.isEmpty();
    }

//    protected void ensureConstrainedMode() {
//        if (vscToRangeSets == null) {
//            vscToRangeSets = new HashMap<>();
//        }
//    }

    public void add(Range<NodeWrapper> range) {
        Object vsc = classifyValueSpace(range);

        if (ValueSpaceClassification.VSPACE_DIFFERENT.equals(vsc) && vscToRangeSets != null) {
            // Inconsistent range - go into conflicting state
            vscToRangeSets.clear();
            this.isVscExhaustive = true;
        } else {
            RangeSet<NodeWrapper> rangeSet = vscToRangeSets.computeIfAbsent(vsc, x -> TreeRangeSet.create());
            rangeSet.add(range);
        }
    }


    public void substract(Range<NodeWrapper> range) {

        if (!isConflicting()) {
            Object vsc = classifyValueSpace(range);

            if (vsc == null) {
                // null means substracting all possible ranges
                vscToRangeSets.clear();
            } else if (ValueSpaceClassification.VSPACE_DIFFERENT.equals(vsc) && vscToRangeSets != null) {
                // substracting a contradicting range does nothing
                // raise exception?
            } else {
                RangeSet<NodeWrapper> rangeSet = vscToRangeSets.computeIfAbsent(vsc, x -> TreeRangeSet.create());

//                RangeSet<NodeWrapper> tmp = TreeRangeSet.create();
//                tmp.add(range);
//                RangeSet<NodeWrapper> substraction = tmp.complement();

                rangeSet.remove(range);
            }
        }
    }

    /**
     * Mutate the ranges of this to create the intersection with other
     *
     * @param that
     * @return
     */
    public NodeRanges stateIntersection(NodeRanges that) {

        if (!this.isVscExhaustive) {
            for (Iterator<Entry<Object, RangeSet<NodeWrapper>>> it = that.vscToRangeSets.entrySet().iterator(); it.hasNext(); ) {
                Entry<Object, RangeSet<NodeWrapper>> e = it.next();
                Object vsc = e.getKey();
                RangeSet<NodeWrapper> thatRangeSet = e.getValue();

                if (!this.vscToRangeSets.containsKey(vsc)) {
                    this.vscToRangeSets.put(vsc, thatRangeSet);
                }
            }
        }

        //for (Entry<Object, RangeSet<ComparableNodeWrapper>> e: vscToRangeSets.entrySet()) {
        for (Iterator<Entry<Object, RangeSet<NodeWrapper>>> it = this.vscToRangeSets.entrySet().iterator(); it.hasNext(); ) {
            Entry<Object, RangeSet<NodeWrapper>> e = it.next();
            Object vsc = e.getKey();
            RangeSet<NodeWrapper> thisRangeSet = e.getValue();

            RangeSet<NodeWrapper> thatRangeSet = that.vscToRangeSets.get(vsc);

            // If there are no other ranges in the value space than the intersection is empty
            if (thatRangeSet == null) {
                if (that.isVscExhaustive) {
                    it.remove();
                } else {
                    // noop
                }
            } else {
                // Intersection by means of removing the other's complement
                // https://github.com/google/guava/issues/1825
                RangeSet<NodeWrapper> thatComplement = thatRangeSet.complement();
                thisRangeSet.removeAll(thatComplement);

                if (thisRangeSet.isEmpty()) {
                    it.remove();
                }
            }
        }

        this.isVscExhaustive = this.isVscExhaustive || that.isVscExhaustive;

        return this;
    }


    /**
     * Add all other ranges to this one
     *
     * @param that
     * @return
     */
    public NodeRanges stateUnion(NodeRanges that) {
        this.isVscExhaustive = this.isVscExhaustive && that.isVscExhaustive;

        for (Iterator<Entry<Object, RangeSet<NodeWrapper>>> it = that.vscToRangeSets.entrySet().iterator(); it.hasNext(); ) {
            Entry<Object, RangeSet<NodeWrapper>> e = it.next();
            Object vsc = e.getKey();

            RangeSet<NodeWrapper> thatRangeSet = e.getValue();
            RangeSet<NodeWrapper> thisRangeSet = this.vscToRangeSets.computeIfAbsent(vsc, x -> TreeRangeSet.create());

            thisRangeSet.addAll(thatRangeSet);

            // In open mode (= not exhaustive) remove all unconstrained ranges
            if (!this.isVscExhaustive && thisRangeSet.complement().isEmpty()) {
                it.remove();
            }
        }

        return this;
    }


    /** Constrain the value space to only the given node - this is an intersection operation */
    public NodeRanges stateValue(Node value) {
        NodeWrapper nw = NodeWrapper.wrap(value);
        Range<NodeWrapper> range = Range.singleton(nw);
        Object vsc = classifyValueSpace(range);

        vscToRangeSets.keySet().retainAll(Collections.singleton(vsc));

        RangeSet<NodeWrapper> ranges = vscToRangeSets.get(vsc);
        // If there is no range we can create one if we are not exhaustive
        // otherwise we are conflicting
        if (ranges == null) {
            if (!isVscExhaustive) {
                ranges = TreeRangeSet.create();
                vscToRangeSets.put(vsc, ranges);
                ranges.add(range);
            } //  else { noop } - exhaustive is true and we already cleared all other value spaces
        } else if (ranges.contains(nw)) {
            ranges.clear();
            ranges.add(range);
        } else {
            vscToRangeSets.clear();
        }

        this.isVscExhaustive = true;
        return this;
    }

    /** Add a constant to its respective value space
     * This is a union-like operation - its does not constrain the space to the given value */
    public NodeRanges addValue(Node value) {
        Range<NodeWrapper> range = Range.singleton(NodeWrapper.wrap(value));
        add(range);
        return this;
    }


    /**
     * Add the negation of a value; e.g. ?x != 5
     * This only excludes the value from its respective value space.
     * Any other value space or whether their enumeration is exhaustive is unaffected.
     *
     * @param value
     * @return True if this is in conflicting state
     */
    public boolean substractValue(Node value) {
        Range<NodeWrapper> range = Range.singleton(NodeWrapper.wrap(value));
        Object vsc = classifyValueSpace(range);

        RangeSet<NodeWrapper> ranges = vscToRangeSets.get(vsc);
        if (ranges == null) {
            if (!isVscExhaustive) {
                ranges = TreeRangeSet.create();
                RangeSet<NodeWrapper> tmp = ranges.complement();
                tmp.remove(range);
                vscToRangeSets.put(vsc, tmp);
            } // else { noop }
        } else {
            ranges.remove(range);

            if (isVscExhaustive && ranges.isEmpty()) {
                vscToRangeSets.remove(vsc);
            }
        }


        return isConflicting();
    }


    public boolean isVscExhaustive() {
        return isVscExhaustive;
    }

    public Set<?> getValueSpaces() {
        return vscToRangeSets.keySet();
    }

    public boolean contains(Node node) {
        return contains(NodeValue.makeNode(node));
    }

    public boolean contains(NodeValue nodeValue) {
        Object vsc = nodeValue.getValueSpace();
        NodeWrapper tmp = NodeWrapper.wrap(nodeValue);
        boolean result = Optional.ofNullable(vscToRangeSets.get(vsc)).map(rangeSet -> rangeSet.contains(tmp)).orElse(!isVscExhaustive);
        return result;
    }

    /** True iff all ranges are singletons; i.e. if this object can be converted into
     * a possible empty enumeration of nodes */
    public boolean isDiscrete() {
        boolean result = vscToRangeSets != null && vscToRangeSets.values().stream()
                .allMatch(RangeUtils::isDiscrete);
        return result;
    }

    public Stream<Node> streamDiscrete() {
        return vscToRangeSets.values().stream().flatMap(rangeSet -> rangeSet.asRanges().stream()
                .map(Range::lowerEndpoint)
                .map(NodeWrapper::getNode));
    }

    /** Returns true if there is exactly one value space with exactly one singleton */
    public boolean isConstant() {
        boolean result = vscToRangeSets != null && vscToRangeSets.size() == 1
                ? RangeUtils.isSingleton(vscToRangeSets.values().iterator().next())
                : false;

        return result;
    }

    /** Always first check {@link #isConstant()} before calling this method */
    public Node getConstant() {
        Node result = vscToRangeSets.values().iterator().next().asRanges().iterator().next().lowerEndpoint().getNode();
        return result;
    }


    /**
     * Return true if this node range subsumes the other one, i.e.
     * this node range is less-or-equal restrictive than the other.
     *
     *
     * @param other
     * @return
     */
    public boolean subsumes(NodeRanges other) {
        boolean result;
        if (other.isUnconstrained()) {
            result = this.isUnconstrained();
        } else {
            if (this.isUnconstrained()) {
                result = true;
            } else {
                result = this.getValueSpaces().containsAll(other.getValueSpaces());

                if (result) {
                    for (Entry<Object, RangeSet<NodeWrapper>> e : this.vscToRangeSets.entrySet()) {
                        Object vsc = e.getKey();
                        RangeSet<NodeWrapper> thisRangeSet = e.getValue();

                        RangeSet<NodeWrapper> otherRangeSet = other.vscToRangeSets.get(vsc);
                        if (otherRangeSet == null) {
                            result = false;
                            break;
                        } else {
                            result = thisRangeSet.enclosesAll(otherRangeSet);
                        }
                    }
                }

            }
        }

        return result;
    }


    @Override
    public String toString() {
        return "NodeRanges [isVscExhaustive=" + isVscExhaustive + ", vscToRangeSets=" + vscToRangeSets + "]";
    }

    public static Object classifyNodeValueSubSpace(Node node) {
        Object result;
        if (node.isURI()) {
            result = VSC_IRI;
        } else if (node.isBlank()) {
            result = VSC_BNODE;
        } else if (node.isNodeTriple()) {
            result = VSC_TRIPLE;
        } else {
            throw new RuntimeException("Unknown term type: " + node);
        }

        return result;
    }

    /** Return some object that acts as a key for a value space. Different value spaces are assumed to be disjoint. */
    public static Object classifyValueSpace(Range<NodeWrapper> range) {
        Object result = null;
        NodeValue lb = range.hasLowerBound() ? range.lowerEndpoint().getNodeValue() : null;
        NodeValue ub = range.hasUpperBound() ? range.upperEndpoint().getNodeValue() : null;

        if (lb != null && ub != null) {
            result = NodeValue.classifyValueOp(lb, ub);

            if (ValueSpaceClassification.VSPACE_NODE.equals(result)) {
                Object a = classifyNodeValueSubSpace(lb.asNode());
                Object b = classifyNodeValueSubSpace(ub.asNode());

                if (!Objects.equals(a, b)) {
                    result = ValueSpaceClassification.VSPACE_DIFFERENT;
                } else {
                    result = a;
                }
            }

        } else if (lb != null) {
            result = lb.getValueSpace();
            if (ValueSpaceClassification.VSPACE_NODE.equals(result)) {
                result = classifyNodeValueSubSpace(lb.asNode());
            }
        } else if (ub != null) {
            result = ub.getValueSpace();
            if (ValueSpaceClassification.VSPACE_NODE.equals(result)) {
                result = classifyNodeValueSubSpace(ub.asNode());
            }
        }

        return result;
    }


    public static void main(String[] args) {
        Node iri = NodeFactory.createURI("http://example.org/foo");

        NodeValue a = NodeValue.makeInteger(100);
        NodeValue b = NodeValue.makeInteger(500);
        NodeValue c = NodeValue.makeString("hi");
        NodeValue d = NodeValue.makeDouble(123.4);
        NodeValue e = NodeValue.makeNode(iri);

        NodeRanges nr = NodeRanges.createOpen();
        System.out.println(nr.isUnconstrained());

        nr.add(Range.singleton(NodeWrapper.wrap(iri)));
        nr.add(Range.singleton(NodeWrapper.wrap(a)));
        System.out.println(nr.contains(b));
        System.out.println(nr.contains(c));

        System.out.println(NodeValue.compare(a, d));

        System.out.println(nr.getValueSpaces());

        // System.out.println(nr.is);
        System.out.println("isDiscrete: " + nr.isDiscrete());
        System.out.println("isConstant: " + nr.isConstant());
    }

}
