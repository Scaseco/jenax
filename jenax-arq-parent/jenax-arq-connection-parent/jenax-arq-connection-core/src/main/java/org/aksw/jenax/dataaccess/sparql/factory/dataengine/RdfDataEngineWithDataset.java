package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import org.aksw.jenax.arq.util.dataset.HasDataset;
import org.aksw.jenax.dataaccess.sparql.dataengine.RdfDataEngine;

public interface RdfDataEngineWithDataset
    extends RdfDataEngine, HasDataset
{
}
