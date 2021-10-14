package org.aksw.jenax.constraint.api;


import java.util.EnumSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.commons.util.range.RangeUtils;
import org.aksw.jenax.arq.rdfterm.RdfTermType;
import org.aksw.jenax.constraint.util.NodeRanges;
import org.aksw.jenax.constraint.util.PrefixSet;
import org.apache.jena.graph.Node;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;




/**
 * This class represents restrictions to be used on variables.
 *
 *
 * Rules
 * . Stating a constant value (node) must be consistent with at least one prefix (if there are any), or equivalent to a previous value.
 *   Additionally all prefixes are removed in that case.
 *
 * . If a restriction is inconsistent, retrieving fields is meaningless, as their values are not defined.
 *
 *
 * .) Methods return true if a change occurred
 *
 * . TODO More details
 *
 *
 * Further statements could be:
 *
 * statePattern()
 * stateRange(min, max)
 * stateDatatype()
 *
 * I really hope I am not ending up with my own Datalog+Constraints engine :/
 *
 *
 * TODO: Maybe the set of uriPrefixes should be replaced with a single prefix -
 * so that an instance of restriction really only states a single restriction.
 *
 * So my problem is how to deal with dis/conjunctions of restrictions efficiently
 *
 *
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
//public class ConstraintImpl
//    implements RdfTermConstraint
//{
//    protected EnumSet<RdfTermType> termTypes;
//
//    /** Only applicable if termTypes includes IRI */
//    protected PrefixSet iriPrefixes;
//
//    /** Valid ranges of values; only applicable for IRI and literal term types*/
//    // Note NodeRanges itself cannot capture negations (e.g. ?x != <foo>)
//    // so negations so far count as unconstrained
//    protected NodeRanges ranges;
//
//    protected boolean isInconsistent = false;
//
//
//    public ConstraintImpl(EnumSet<RdfTermType> termTypes) {
//        this.termTypes = termTypes;
//    }
//
//    public ConstraintImpl clone() {
//        return new ConstraintImpl(this);
//    }
//
//    public ConstraintImpl() {
//        isInconsistent = Boolean.TRUE;
//    }
//
//
//
//
//    /**
//     * Return true if 'this' is equal to or less restrictive than other
//     *
//     * @param other
//     * @return
//     */
//    public boolean subsumesOrIsEqual(ConstraintImpl other) {
//        boolean result = true;
//
//        if (!termTypes.containsAll(other.termTypes)) {
//            result = false;
//        } else if (!this.ranges.subsumes(other.ranges)) {
//            result = false;
//        }
//
//        if(result && iriPrefixes != null) {
//            // Check of on of the prefixes is a prefix of the constant
//            if(other.node != null) {
//                if(this.uriPrefixes.containsPrefixOf(other.node.toString())) {
//                    return true;
//                }
//            }
//
//            if(other.getUriPrefixes() == null) {
//                return false;
//            } else {
//                // Test whether each of this.prefixes is a prefix of other
//                for(String prefix : other.getUriPrefixes().getSet()) {
//                    if(!this.uriPrefixes.containsPrefixOf(prefix)) {
//                        return false;
//                    }
//                }
//            }
//        }
//
//
//        return true;
//    }
//
//
//
//
//    public boolean hasConstant() {
//        return !isContradicting() && ranges.isConstant();
//    }
//
//    public boolean hasPrefix() {
//        return uriPrefixes != null;
//    }
//
//
//    public EnumSet<RdfTermType> getRdfTermTypes() {
//        return termTypes;
//    }
//
//
//    public Node getNode() {
//        return ranges.getConstant();
//    }
//
//    public PrefixSet getIriPrefixes() {
//        return iriPrefixes;
//    }
//
//
//
//    /* (non-Javadoc)
//     * @see org.aksw.sparqlify.restriction.IRestriction#stateRestriction(org.aksw.sparqlify.restriction.Restriction)
//     */
//    @Override
//    public boolean stateRestriction(ConstraintImpl that) {
//
//
//
//        if(isInconsistent == Boolean.FALSE) {
//            return false;
//        } else if(other.node != null) {
//            return stateNode(other.node);
//        } else if(other.uriPrefixes != null) {
//            return stateUriPrefixes(other.uriPrefixes);
//        } else if(other.getType() != RdfTermType.UNKNOWN) {
//            return stateType(other.type);
//        }
//
//        throw new RuntimeException("Should not happen");
//    }
//
//
//    /* (non-Javadoc)
//     * @see org.aksw.sparqlify.restriction.IRestriction#stateType(org.aksw.sparqlify.restriction.Type)
//     */
//    @Override
//    public boolean stateType(RdfTermType newType) {
//        return stateTypes(EnumSet.of(newType));
//    }
//
//    public boolean stateTypes(EnumSet<RdfTermType> rdfTermTypes) {
//        if (isInconsistent) {
//            return false;
//        }
//
//        termTypes.retainAll(rdfTermTypes);
//
//        // isInconsistent =
//    }
//
//
//    /* (non-Javadoc)
//     * @see org.aksw.sparqlify.restriction.IRestriction#stateNode(org.apache.jena.graph.Node)
//     */
//    @Override
//    public boolean stateNode(Node newNode) {
//        boolean change = stateType(getNodeType(newNode));
//
//        if(isInconsistent == Boolean.FALSE) {
//            return change;
//        }
//
//        if(node == null) {
//            if(uriPrefixes != null) {
//                /*
//                if(!node.isURI()) {
//                    satisfiability = Boolean.FALSE;
//                    return true;
//                }*/
//
//                if(!uriPrefixes.containsPrefixOf(newNode.getURI())) {
//                    isInconsistent = Boolean.FALSE;
//                    return true;
//                }
//            }
//
//            node = newNode;
//            isInconsistent = null;
//
//            return true;
//
//        } else {
//
//            if(!node.equals(newNode)) {
//                isInconsistent = Boolean.FALSE;
//                return true;
//            }
//
//            return false;
//        }
//    }
//
//    @Override
//    public boolean stateIriPrefixes(PrefixSet other) {
//
//        this.iriPrefixes.intersect(other);
//
//        RangeSet<NodeWrapper> iriRanges = this.ranges.getIriRanges();
//
//        // Filter out non-matching singletons
//        for (Iterator<Range<NodeWrapper>> it = iriRanges.asRanges().iterator(); it.hasNext(); ) {
//            Range<NodeWrapper> e = it.next();
//            if (RangeUtils.isSingleton(e)) {
//                String iri = e.lowerEndpoint().getNode().getURI();
//                if (iriPrefixes.getPrefixesOf(iri, true).isEmpty()) {
//                    it.remove();
//                }
//            }
//        }
//
//        // TODO Include ranges?
//
//
//
//        if(prefixes.isEmpty()) {
//            throw new RuntimeException("Should not happen");
//        }
//
//        boolean change = stateType(RdfTermType.IRI);
//
//
//
//        if(isInconsistent == Boolean.FALSE) {
//            return change;
//        }
//
//
//        if(node != null) {
//            if(!node.isURI() || !prefixes.containsPrefixOf(node.getURI())) {
//                isInconsistent = Boolean.FALSE;
//
//                return true;
//            }
//
//            // We have a constant, no need to track the prefixes
//            return false;
//        }
//
//        // If no prefixes have been stated yet, state them
//        if(uriPrefixes == null) {
//            uriPrefixes = new PrefixSet();
//            for(String s : prefixes.getSet()) {
//                Set<String> ps = uriPrefixes.getPrefixesOf(s);
//                uriPrefixes.removeAll(ps);
//                uriPrefixes.add(s);
//            }
//
//            isInconsistent = uriPrefixes.isEmpty() ? false : null;
//            return true;
//        } else if(prefixes.isEmpty()) {
//
//            // If we get here, then we were not inconsistent yet
//            // TODO Not sure if the satisfiability computation also works for TRUE
//            if(uriPrefixes.isEmpty()) {
//                isInconsistent = Boolean.FALSE;
//                return true;
//            } else {
//                return false;
//            }
//        }
//
//        // {http:, mailto:addr} {http://foo, mailto:}
//
//        // Note: If we have prefixes Foo and FooBar, we keep FooBar, which is more restrictive.
//        for(String s : prefixes.getSet()) {
//            Set<String> ps = uriPrefixes.getPrefixesOf(s, false);
//            if(!ps.isEmpty()) {
//                uriPrefixes.removeAll(ps);
//                uriPrefixes.add(s);
//            }
//        }
//
//        // Remove all entries that do not have a prefix in the other set
//        Iterator<String> it = uriPrefixes.getSet().iterator();
//        while(it.hasNext()) {
//            String s = it.next();
//            Set<String> ps = prefixes.getPrefixesOf(s);
//            if(ps.isEmpty()) {
//                it.remove();
//            }
//        }
//
//        if(uriPrefixes.isEmpty()) {
//            isInconsistent = Boolean.FALSE;
//            return true;
//        }
//
//        // TODO Could sometimes return false
//        return true;
//        //return change;
//    }
//
//
//
//    @Override
//    public boolean isContradicting() {
//        return isInconsistent;
//    }
//
//
//
//
////  // If no prefixes have been stated yet, state them
////  if(uriPrefixes == null) {
////      uriPrefixes = new PrefixSet();
////      for(String s : prefixes.getSet()) {
////          Set<String> ps = uriPrefixes.getPrefixesOf(s);
////          uriPrefixes.removeAll(ps);
////          uriPrefixes.add(s);
////      }
////
////      isInconsistent = uriPrefixes.isEmpty() ? false : null;
////      return true;
////  } else if(prefixes.isEmpty()) {
////
////      // If we get here, then we were not inconsistent yet
////      // TODO Not sure if the satisfiability computation also works for TRUE
////      if(uriPrefixes.isEmpty()) {
////          isInconsistent = Boolean.FALSE;
////          return true;
////      } else {
////          return false;
////      }
////  }
//
//
//    // To be done.
//    /* (non-Javadoc)
//     * @see org.aksw.sparqlify.restriction.IRestriction#statePattern(com.karneim.util.collection.regex.PatternPro)
//     */
////    @Override
////    public void statePattern(PatternPro pattern) {
////        // If there is a pattern already, make it the intersection with the new pattern
////
////        // If there is a node, check if it conforms to the pattern
////
////        // If there are prefixes, check if they conform to the pattern
////
////        throw new NotImplementedException();
////    }
//
//}