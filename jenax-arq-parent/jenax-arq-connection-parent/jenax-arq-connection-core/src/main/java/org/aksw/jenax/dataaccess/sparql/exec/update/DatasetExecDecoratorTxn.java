package org.aksw.jenax.dataaccess.sparql.exec.update;

import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.UpdateExec;

public class DatasetExecDecoratorTxn<T extends UpdateExec>
    extends UpdateExecDecoratorTxn<T> implements UpdateExec
{
    public DatasetExecDecoratorTxn(T decoratee, Transactional transactional) {
        super(decoratee, transactional);
    }
}
