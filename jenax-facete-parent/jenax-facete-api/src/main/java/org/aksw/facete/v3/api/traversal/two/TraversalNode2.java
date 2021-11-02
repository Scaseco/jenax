package org.aksw.facete.v3.api.traversal.two;

/**
 * Simple traversal via predicates.
 * There is no support for aliases.
 *
 * @author raven
 *
 * @param <N>
 * @param <D>
 */
//public interface TraversalNode2<
//    N extends TraversalNode2<N, D>,
//    D extends TraversalDirNode2<N>>
//{
//    D fwd();
//    D bwd();
//
//    //BgpNode model();
//
//
//    default boolean canOpt() {
//        return false;
//    }
//
//    /**
//     * Return a wrapping of this traversal node which makes the next step optional
//     *
//     * @return
//     */
//    default M opt() {
//        throw new UnsupportedOperationException("Optional traversal not implemented or not overridden");
//    }
//
//    // Convenience shortcuts
//    default M fwd(Property property) {
//        return fwd().via(property);
//    }
//
//    default M bwd(Property property) {
//        return bwd().via(property);
//    }
//
//    default M fwd(String p) {
//        Property property = ResourceFactory.createProperty(p);
//        return fwd().via(property);
//    }
//
//    default M bwd(String p) {
//        Property property = ResourceFactory.createProperty(p);
//        return bwd().via(property);
//    }
//
//    default M fwd(Node node) {
//        return fwd().via(ResourceFactory.createProperty(node.getURI()));
//    }
//
//    default M bwd(Node node) {
//        return bwd().via(ResourceFactory.createProperty(node.getURI()));
//    }
//
//    default D step(Direction direction) {
//        return BACKWARD.equals(direction) ? bwd() : fwd();
//    }
//
//    default M step(String p, Direction direction) {
//        return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
//    }
//
//    default M step(Node p, Direction direction) {
//        return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
//    }
//
//    default M step(Property p, Direction direction) {
//        return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
//    }
//
////	default N step(AliasedPathStep aliasedStep) {
////		throw new RuntimeException("Not implemented");
////	}
//
////	default N step(P_Path0 p, String alias) {
////		boolean isFwd = p.isForward();
////		Node node = p.getNode();
////		M tmp = isFwd ? fwd(node) : bwd(node);
////		N result = tmp.viaAlias(alias);
////		return result;
////	}
//
//    default N walk(AliasedPath path) {
//        List<Entry<P_Path0, String>> steps = path.getSteps();
////		if(true) {
////			throw new RuntimeException("API broke here");
////		}
////		N result = null;
//        N result = walkAliased(steps.iterator());
//        return result;
//    }
//
//    default N walkAliased(Iterator<? extends Entry<P_Path0, String>> it) {
//        N result;
//        if(it.hasNext()) {
//            Entry<P_Path0, String> step = it.next();
//            P_Path0 p = step.getKey();
//            String alias = step.getValue();
////			N next = step(step);
//            N next = step(p.getNode(), Direction.ofFwd(p.isForward())).viaAlias(alias);
//            result = next.walkAliased(it);
//        } else {
//            result = (N)this;
//        }
//
//        return result;
//    }
//
//    default N walk(Path path) {
//        TraversalNode<N, D, M> result;
//        if(path == null) {
//            result = this;
//        } else if(path instanceof P_Seq) {
//            P_Seq seq = (P_Seq)path;
//            result = walk(seq.getLeft()).walk(seq.getRight());
//        } else if(path instanceof P_Link) {
//            P_Link link = (P_Link)path;
//            result = fwd(link.getNode()).one();
//        } else if(path instanceof P_ReverseLink) {
//            P_ReverseLink reverseLink = (P_ReverseLink)path;
//            result = bwd(reverseLink.getNode()).one();
//        } else {
//            throw new IllegalArgumentException("Unsupported path type " + path + " " + Optional.ofNullable(path).map(Object::getClass).orElse(null));
//        }
//
//        return (N) result;
//    }
//
//    default N walk(SimplePath simplePath) {
//        return walk(SimplePath.toPropertyPath(simplePath));
//    }
//}
