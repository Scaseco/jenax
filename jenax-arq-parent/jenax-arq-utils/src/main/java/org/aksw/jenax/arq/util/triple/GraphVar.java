package org.aksw.jenax.arq.util.triple;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import com.google.common.collect.BiMap;

public interface GraphVar
    extends Graph
{
    BiMap<Var, Node> getVarToNode();

    // The underlying graph without the variable substitutions
    Graph getWrapped();

    default BiMap<Node, Var> getNodeToVar() {
        return getVarToNode().inverse();
    }

//    // Test if a node is mapped to a variable
//    default boolean isVar(Node outer) {
//    	BiMap<Var, Node> map = getVarToNode();
//    	
//    }
}
