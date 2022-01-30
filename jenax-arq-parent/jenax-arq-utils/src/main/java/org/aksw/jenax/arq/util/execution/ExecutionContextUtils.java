package org.aksw.jenax.arq.util.execution;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeFactoryExtra;

public class ExecutionContextUtils {
	
	/** Creates an execution context primarily for use as a FunctionEnv */
	public static ExecutionContext createFunctionEnv() {
        Context context = ARQ.getContext().copy();
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime());
        ExecutionContext result = new ExecutionContext(context, null, null, null);
        return result;
	}

	/** Creates an execution context based on a given dataset graph and a derived default OpExecutorFactory */
	public static ExecutionContext createExecCxt(DatasetGraph dsg) {
        Context context = ARQ.getContext().copy() ;
        context.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime()) ;
        ExecutionContext result = new ExecutionContext(context, dsg.getDefaultGraph(), dsg, QC.getFactory(context)) ;
        return result;
	}
	
	/** Same as {@link #createExecCxt(DatasetGraph)} with an empty default dataset graph */
	public static ExecutionContext createExecCxtEmptyDsg() {
		return createExecCxt(DatasetGraphFactory.create());
	}

}
