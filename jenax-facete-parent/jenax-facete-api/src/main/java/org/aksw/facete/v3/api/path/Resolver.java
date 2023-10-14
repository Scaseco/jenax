package org.aksw.facete.v3.api.path;

import java.util.Collection;

import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.jena_sparql_api.relationlet.RelationletBinary;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.apache.jena.sparql.path.P_Path0;

public interface Resolver {
    //List<P_Path0> getPath();
    Resolver getParent();
//	P_Path0 getReachingStep();

    Resolver resolve(P_Path0 step, String alias);

    Collection<RelationletBinary> getReachingRelationlet();

    /**
     * Return this resolver's specification
     * of the RDF graph reached in forward or backward direction.
     * Evaluation of each of the returned {@link Fragment3}s over an RDF graph
     * yields this spec's corresponding RDF graph.
     *
     *
     *
     * @param fwd
     * @return
     */
    Collection<Fragment3> getRdfGraphSpec(boolean fwd);

    default Resolver resolve(P_Path0 step) {
        Resolver result = resolve(step, null);
        return result;
    }

    default Resolver getRoot() {
        Resolver result = TreeUtils.getRoot(this, Resolver::getParent);
//		Resolver result = Traverser.<Resolver>forTree(x -> Collections.singleton(x.getParent()))
//				.depthFirstPostOrder(this).iterator().next();
        return result;
    }

//	BinaryRelation getBinaryRelation(boolean fwd);

    @Deprecated
    default Collection<Fragment2> getPaths() {
        throw new UnsupportedOperationException("This method should no longer be used");
    }
}