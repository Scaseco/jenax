package org.aksw.jenax.arq.fromasfilter.engine;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphWrapper;

/** This wrapper does not contain custom logic. It is just a marker that
 * gets picked up by */
public class DatasetGraphFromAsFilter
    extends DatasetGraphWrapper
{
    public DatasetGraphFromAsFilter(DatasetGraph dsg) {
        super(dsg);
    }
}
