package org.aksw.facete.v3.api.traversal;

import static org.aksw.facete.v3.api.Direction.BACKWARD;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.aksw.facete.v3.api.AliasedPath;
import org.aksw.facete.v3.api.Direction;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.path.SimplePath;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.Path;


/**
 * Mixin for Node Navigation methods
 */
public interface TraversalNode<
    N extends TraversalNode<N,D,M>,
    D extends TraversalDirNode<N, M>,
    M extends TraversalMultiNode<N>>
//extends PathTraitNode<M>
{
    D fwd();
    D bwd();

    //BgpNode model();


    @Deprecated // All steps are optional; adding a 'bound' constraint to a paths makes them mandatory!
    default boolean canOpt() {
        return false;
    }

    /**
     * Return a wrapping of this traversal node which makes the next step optional
     *
     * @return
     */
    @Deprecated
    default M opt() {
        throw new UnsupportedOperationException("Optional traversal not implemented or not overridden");
    }

    // Convenience shortcuts
    default M fwd(Property property) {
        return fwd().via(property);
    }

    default M bwd(Property property) {
        return bwd().via(property);
    }

    default M fwd(String p) {
        Property property = ResourceFactory.createProperty(p);
        return fwd().via(property);
    }

    default M bwd(String p) {
        Property property = ResourceFactory.createProperty(p);
        return bwd().via(property);
    }

    default M fwd(Node node) {
        return fwd().via(ResourceFactory.createProperty(node.getURI()));
    }

    default M bwd(Node node) {
        return bwd().via(ResourceFactory.createProperty(node.getURI()));
    }

    default D step(Direction direction) {
        return BACKWARD.equals(direction) ? bwd() : fwd();
    }

    default M step(String p, Direction direction) {
        return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
    }

    default M step(Node p, Direction direction) {
        return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
    }

    default M step(Property p, Direction direction) {
        return BACKWARD.equals(direction) ? bwd(p) : fwd(p);
    }

    default N step(FacetStep step) {
        // TODO We implicitly assume 'TARGET' for step.getTargetComponent() - validate!
        return step(step.getNode(), Direction.ofFwd(step.getDirection().isForward())).viaAlias(step.getAlias());
    }

//	default N step(AliasedPathStep aliasedStep) {
//		throw new RuntimeException("Not implemented");
//	}

//	default N step(P_Path0 p, String alias) {
//		boolean isFwd = p.isForward();
//		Node node = p.getNode();
//		M tmp = isFwd ? fwd(node) : bwd(node);
//		N result = tmp.viaAlias(alias);
//		return result;
//	}

    @Deprecated
    default N walk(AliasedPath path) {
        List<Entry<P_Path0, String>> steps = path.getSteps();
//		if(true) {
//			throw new RuntimeException("API broke here");
//		}
//		N result = null;
        N result = walkAliased(steps.iterator());
        return result;
    }

    @Deprecated
    default N walkAliased(Iterator<? extends Entry<P_Path0, String>> it) {
        N result;
        if(it.hasNext()) {
            Entry<P_Path0, String> step = it.next();
            P_Path0 p = step.getKey();
            String alias = step.getValue();
//			N next = step(step);
            N next = step(p.getNode(), Direction.ofFwd(p.isForward())).viaAlias(alias);
            result = next.walkAliased(it);
        } else {
            result = (N)this;
        }

        return result;
    }

    @Deprecated
    default N walk(Path path) {
        TraversalNode<N, D, M> result;
        if(path == null) {
            result = this;
        } else if(path instanceof P_Seq) {
            P_Seq seq = (P_Seq)path;
            result = walk(seq.getLeft()).walk(seq.getRight());
        } else if(path instanceof P_Link) {
            P_Link link = (P_Link)path;
            result = fwd(link.getNode()).one();
        } else if(path instanceof P_ReverseLink) {
            P_ReverseLink reverseLink = (P_ReverseLink)path;
            result = bwd(reverseLink.getNode()).one();
        } else {
            throw new IllegalArgumentException("Unsupported path type " + path + " " + Optional.ofNullable(path).map(Object::getClass).orElse(null));
        }

        return (N) result;
    }

    @Deprecated
    default N walk(SimplePath simplePath) {
        return walk(SimplePath.toPropertyPath(simplePath));
    }
}
