package org.aksw.jenax.arq.util.expr;

import java.util.List;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;
import org.apache.jena.sparql.function.FunctionEnv;

public abstract class FunctionBase1WithEnv
	extends FunctionBase
{
	@Override
	public void checkBuild(String uri, ExprList args)
	{
	    if ( args.size() != 1 )
	        throw new QueryBuildException("Function '"+Lib.className(this)+"' takes one argument") ;
	}

	protected NodeValue exec(List<NodeValue> args, FunctionEnv env) {
	    if ( args == null )
	        // The contract on the function interface is that this should not happen.
	        throw new ARQInternalErrorException("FunctionBase1: Null args list") ;

	    if ( args.size() != 1 )
	        throw new ExprEvalException("FunctionBase1: Wrong number of arguments: Wanted 1, got "+args.size()) ;

	    NodeValue v1 = args.get(0) ;

	    return exec(v1, env) ;
    }

	@Override
    public NodeValue exec(List<NodeValue> args) {
        throw new ARQInternalErrorException("Should not be called") ;
    }

	public abstract NodeValue exec(NodeValue v, FunctionEnv env);
}
