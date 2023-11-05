package org.aksw.facete.v3.bgp.api;

import static org.aksw.facete.v3.api.Direction.BACKWARD;
import static org.aksw.facete.v3.api.Direction.FORWARD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aksw.facete.v3.api.Direction;
import org.aksw.facete.v3.api.traversal.TraversalNode;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.aksw.jenax.sparql.path.PathUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.ElementGroup;

public interface BgpNode
    extends TraversalNode<BgpNode, BgpDirNode, BgpMultiNode>,
    Resource
{
    BgpDirNode fwd();
    BgpDirNode bwd();

    Map<Resource, BgpMultiNode> fwdMultiNodes();
    Map<Resource, BgpMultiNode> bwdMultiNodes();

//	default List<P_Path0> toSparqlPath() {
//
//		parent().isReverse()
//		parent().reachingProperty();
//	}

    public BgpNode chRoot();

    public static P_Path0 toStep(BgpMultiNode node) {
        P_Path0 result = FORWARD.equals(node.getDirection())
                ? new P_Link(node.reachingProperty().asNode())
                : new P_ReverseLink(node.reachingProperty().asNode());
        return result;
    }

    public static P_Path0 toStep(BgpNode node) {
        return Optional.ofNullable(node.parent()).map(BgpNode::toStep).orElse(null);
    }

    public static Path toSparqlPath(BgpNode node) {
        List<P_Path0> steps = toSparqlSteps(node);
        Path result = PathUtils.toSparqlPath(steps);
        return result;
    }

    public static List<P_Path0> toSparqlSteps(BgpNode node) {
        List<P_Path0> result = new ArrayList<>();
        P_Path0 tmp;
        while(node != null && (tmp = toStep(node)) != null) {
            result.add(tmp);
            node = node.parent().parent();
        }

        Collections.reverse(result);
        return result;
    }

    BgpNode as(String varName);
    BgpNode as(Var var);
    String alias();


    BgpMultiNode parent();
    //BgpNode parent(BgpMultiNode parent);

    //BinaryRelation getReachingRelation();

    BgpNode root();

    /** Get the set of simple constraints affecting this facet.
     * Simple constraints are expressions making use of only a single variable.
     * The set of constraints is treated as a disjunction */
    //Set<Expr> getConstraints();



    public static Fragment2 getReachingRelation(BgpNode state) {
        Fragment2 result;

        BgpMultiNode parent = state.parent();
        if(parent == null) {
            result = new Fragment2Impl(new ElementGroup(), Vars.s, Vars.o);
        } else {

//			boolean isReverse = false;
//			Resource entry = ResourceUtils.tryGetReversePropertyValue(parent, Vocab.fwd)
//				.orElseGet(() -> ResourceUtils.getReversePropertyValue(parent, Vocab.bwd));
//
//			Resource p = ResourceUtils.getPropertyValue(entry, Vocab.property, Resource.class);

            Resource p = parent.reachingProperty();
            Direction dir = parent.getDirection();

            //Set<Statement> set = ResourceUtils.listProperties(parent, null).filterKeep(stmt -> stmt.getObject().equals(state)).toSet();
//
//			if(set.isEmpty()) {
//				isReverse = true;
//				set = ResourceUtils.listReverseProperties(parent, null).filterKeep(stmt -> stmt.getSubject().equals(state)).toSet();
//			}
//
//			// TODO Should never fail - but ensure that
//			Property p = set.iterator().next().getPredicate();
//
            result = create(p.asNode(), dir);
        }

        return result;
    }

    public static Fragment2 create(Node node, Direction dir) {
        //ElementUtils.createElement(triple)
        Triple t = BACKWARD.equals(dir)
                ? new Triple(Vars.o, node, Vars.s)
                : new Triple(Vars.s, node, Vars.o);

        Fragment2 result = new Fragment2Impl(ElementUtils.createElement(t), Vars.s, Vars.o);
        return result;
    }

}
