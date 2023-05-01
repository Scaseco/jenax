package org.aksw.jenax.constraint.util;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.arq.util.node.ComparableNodeValue;
import org.aksw.jenax.constraint.api.Contradictable;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.ValueSpace;

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
 * FIXME Extend the API with meta-interfaces. e.g. Dimension classes (for numeric, string, iri, etc) could check whether values
 * are within their range.
 *
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
    extends VSpaceBase<ComparableNodeValue, Object>
    implements Contradictable, Cloneable
{
    // Additional (pseudo) value space classifications for uniform handing of IRIs and bnodes
    // No longer needed since Jena 4.8.0-SNAPSHOT - We could now replace Object with ValueSpace
//    public static final String VSC_IRI = "xVSPACE_IRI";
//    public static final String VSC_BNODE = "xVSPACE_BNODE";
//    public static final String VSC_TRIPLE = "xVSPACE_TRIPLE";


    public NodeRanges(boolean isVscExhaustive) {
        super(isVscExhaustive);
    }

    public NodeRanges(boolean isVscExhaustive, Map<Object, RangeSet<ComparableNodeValue>> vscToRangeSets) {
        super(isVscExhaustive, vscToRangeSets);
    }


    public RangeSet<ComparableNodeValue> getIriRanges() {
        return vscToRangeSets == null
                ? ImmutableRangeSet.of()
                : vscToRangeSets.getOrDefault(ValueSpace.VSPACE_URI, ImmutableRangeSet.of());
    }

    /**
     * Create an independent copy of this object
     */
    @Override
    public NodeRanges clone() {
        Map<Object, RangeSet<ComparableNodeValue>> clone = vscToRangeSets.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        e -> TreeRangeSet.create(e.getValue())));
        return new NodeRanges(isVscExhaustive, clone);
    }

    /** Create a NodeRange that contains everything */
    public static NodeRanges createOpen() {
        return new NodeRanges(false);
    }

    /** Create a NodeRange that contains nothing */
    public static NodeRanges createClosed() {
        return new NodeRanges(true);
    }

    @Override
    public NodeRanges addEmptyDimension(Object dimension) {
        super.addEmptyDimension(dimension);
        return this;
    }

    @Override
    public NodeRanges addOpenDimension(Object dimension) {
        super.addOpenDimension(dimension);
        return this;
    }

//    protected void ensureConstrainedMode() {
//        if (vscToRangeSets == null) {
//            vscToRangeSets = new HashMap<>();
//        }
//    }


    /** Constrain the value space to only the given node - this is an intersection operation */
    public NodeRanges stateValue(Node value) {
        ComparableNodeValue nw = ComparableNodeValue.wrap(value);
        Range<ComparableNodeValue> range = Range.singleton(nw);
        Object vsc = classifyValueSpace(range);

        vscToRangeSets.keySet().retainAll(Collections.singleton(vsc));

        RangeSet<ComparableNodeValue> ranges = vscToRangeSets.get(vsc);
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
        Range<ComparableNodeValue> range = Range.singleton(ComparableNodeValue.wrap(value));
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
        Range<ComparableNodeValue> range = Range.singleton(ComparableNodeValue.wrap(value));
        Object vsc = classifyValueSpace(range);

        RangeSet<ComparableNodeValue> ranges = vscToRangeSets.get(vsc);
        if (ranges == null) {
            if (!isVscExhaustive) {
                ranges = TreeRangeSet.create();
                RangeSet<ComparableNodeValue> tmp = ranges.complement();
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
        ComparableNodeValue tmp = ComparableNodeValue.wrap(nodeValue);
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
                .map(ComparableNodeValue::getNode));
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
                    for (Entry<Object, RangeSet<ComparableNodeValue>> e : this.vscToRangeSets.entrySet()) {
                        Object vsc = e.getKey();
                        RangeSet<ComparableNodeValue> thisRangeSet = e.getValue();

                        RangeSet<ComparableNodeValue> otherRangeSet = other.vscToRangeSets.get(vsc);
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
        return (isVscExhaustive ? "closed" : "open") + vscToRangeSets;
    }

//    public static Object classifyNodeValueSubSpace(Node node) {
//        Object result;
//        if (node.isURI()) {
//            result = VSC_IRI;
//        } else if (node.isBlank()) {
//            result = VSC_BNODE;
//        } else if (node.isNodeTriple()) {
//            result = VSC_TRIPLE;
//        } else {
//            throw new RuntimeException("Unknown term type: " + node);
//        }
//
//        return result;
//    }

    @Override
    protected Object classifyValueSpace(Range<ComparableNodeValue> range) {
        return classifyValueSpaceCore(range);
    }

    /** Return some object that acts as a key for a value space. Different value spaces are assumed to be disjoint. */
    public static ValueSpace classifyValueSpaceCore(Range<ComparableNodeValue> range) {
        ValueSpace result = null;
        NodeValue lb = range.hasLowerBound() ? range.lowerEndpoint().getNodeValue() : null;
        NodeValue ub = range.hasUpperBound() ? range.upperEndpoint().getNodeValue() : null;

        if (lb != null && ub != null) {
            result = NodeValue.classifyValueOp(lb, ub);

//            if (ValueSpace.VSPACE_NODE.equals(result)) {
//                Object a = classifyNodeValueSubSpace(lb.asNode());
//                Object b = classifyNodeValueSubSpace(ub.asNode());
//
//                if (!Objects.equals(a, b)) {
//                    result = ValueSpace.VSPACE_DIFFERENT;
//                } else {
//                    result = a;
//                }
//            }

        } else if (lb != null) {
            result = lb.getValueSpace();
//            if (ValueSpace.VSPACE_NODE.equals(result)) {
//                result = classifyNodeValueSubSpace(lb.asNode());
//            }
        } else if (ub != null) {
            result = ub.getValueSpace();
//            if (ValueSpace.VSPACE_NODE.equals(result)) {
//                result = classifyNodeValueSubSpace(ub.asNode());
//            }
        }

        return result;
    }

    public static NodeRanges nodeRangesForPrefix(String prefix) {
        Range<ComparableNodeValue> range = rangeForStringPrefix(prefix);
        NodeRanges result = NodeRanges.createClosed();
        result.add(range);
        return result;
    }

    /** Result is a string range (not IRI) */
    public static Range<ComparableNodeValue> rangeForStringPrefix(String prefix) {
        return Range.closedOpen(
            ComparableNodeValue.wrap(NodeFactory.createLiteral(prefix)),
            ComparableNodeValue.wrap(NodeFactory.createLiteral(incrementLastCharacter(prefix))));
    }

    /**
     * Increment the last character of a string.
     * Useful for defining the upper bound of a range of strings with a certain prefix.
     *
     * TODO We should better represent string using bytes (or code points)
     */
    public static String incrementLastCharacter(String str) {
        int i = str.length() - 1;

        String result;
        if (i < 0) {
            result = str;
        } else {
            char lastChar = str.charAt(i);
            char nextChar = (char)(lastChar + 1);
            result = str.substring(0, i) + nextChar;
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

        nr.add(Range.singleton(ComparableNodeValue.wrap(iri)));
        nr.add(Range.singleton(ComparableNodeValue.wrap(a)));
        System.out.println(nr.contains(b));
        System.out.println(nr.contains(c));

        System.out.println(NodeValue.compare(a, d));

        System.out.println(nr.getValueSpaces());

        // System.out.println(nr.is);
        System.out.println("isDiscrete: " + nr.isDiscrete());
        System.out.println("isConstant: " + nr.isConstant());
    }

}
