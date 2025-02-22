package org.aksw.jenax.dataaccess.sparql.linksource.track;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;
import org.apache.jena.sparql.core.DatasetGraphWrapperView;
import org.apache.jena.sparql.util.Context;

public class DatasetGraphWithExecTracker
    extends DatasetGraphWrapper
    implements DatasetGraphWrapperView
{
    public static DatasetGraph wrap(DatasetGraph dsg) {
        // Put an exec tracker into the dataset's context.
        Context context = dsg.getContext();
        ExecTracker.ensureTracker(context);
        return new DatasetGraphWithExecTracker(dsg);
    }

    protected DatasetGraphWithExecTracker(DatasetGraph dsg) {
        super(dsg);
    }
}
