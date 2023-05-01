package org.aksw.jenax.connection.update;

import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.UpdateExec;

public class DatasetExecDecoratorTxn<T extends UpdateExec>
    extends UpdateProcessorDecoratorTxn<T> implements UpdateExec
{
    public DatasetExecDecoratorTxn(T decoratee, Transactional transactional) {
        super(decoratee, transactional);
    }
}
