package org.aksw.jena_sparql_api.sparql.ext.sys;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

/** Function that prints it's argument to stderr (debugging aid) */
public class FN_PrintErr extends FunctionBase1 {
    @Override
    public NodeValue exec(NodeValue nv) {
        String s = nv.asString() ;
        System.err.println(s) ;
        System.err.flush() ;
        return NodeValue.TRUE ;
    }
}
