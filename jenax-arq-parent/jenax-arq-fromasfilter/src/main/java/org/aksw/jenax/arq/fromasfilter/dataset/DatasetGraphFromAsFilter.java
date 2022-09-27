package org.aksw.jenax.arq.fromasfilter.dataset;

import org.aksw.jenax.arq.fromasfilter.engine.QueryEngineFactoryFromAsFilter;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;

/** This wrapper does not contain any custom logic. It is just a marker that
 * gets picked up by {@link QueryEngineFactoryFromAsFilter}. */
public class DatasetGraphFromAsFilter
    extends DatasetGraphWrapper
{
    public DatasetGraphFromAsFilter(DatasetGraph dsg) {
        super(dsg);
    }
}
