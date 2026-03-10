package org.aksw.jenax.arq.util.exec.query;

import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.util.Context;

public class ExecutionContextUtils {

    /** Creates an execution context primarily for use as a FunctionEnv */
    public static ExecutionContext createFunctionEnv() {
        Context context = ARQ.getContext().copy();
        Context.setCurrentDateTime(context);
        ExecutionContext result = ExecutionContext.create(context);
        return result;
    }

    /** Creates an execution context based on a given dataset graph and a derived default OpExecutorFactory */
    public static ExecutionContext createExecCxt(DatasetGraph dsg) {
        Context context = ARQ.getContext().copy() ;
        // OpExecutorFactory opExecutorFactory = QC.getFactory(context);
        // QC.setFactory(context, QC.getFactory(dsg.getContext()));
        Context.setCurrentDateTime(context);
        ExecutionContext result = ExecutionContext.create(dsg, context);
        return result;
    }

    public static ExecutionContext createExecCxtEmptyDsg(Context context) {
        DatasetGraph dsg = DatasetGraphFactory.create();
        Context.setCurrentDateTime(context);
        ExecutionContext result = ExecutionContext.create(dsg, context);
        return result;
    }

    /** Same as {@link #createExecCxt(DatasetGraph)} with an empty default dataset graph */
    public static ExecutionContext createExecCxtEmptyDsg() {
        return createExecCxt(DatasetGraphFactory.create());
    }
}
