package org.aksw.jenax.dataaccess.sparql.exec.update;

import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.UpdateExec;

public class DatasetExecWrapperTxn<T extends UpdateExec>
    extends UpdateExecWrapperTxn<T> implements UpdateExec
{
    public DatasetExecWrapperTxn(T decoratee, Transactional transactional) {
        super(decoratee, transactional);
    }
}
