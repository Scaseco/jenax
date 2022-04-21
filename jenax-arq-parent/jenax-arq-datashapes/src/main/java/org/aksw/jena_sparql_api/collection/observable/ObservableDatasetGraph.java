package org.aksw.jena_sparql_api.collection.observable;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;

import org.aksw.commons.collection.observable.Registration;
import org.apache.jena.sparql.core.DatasetGraph;

public interface ObservableDatasetGraph
    extends DatasetGraph
{
    Runnable addVetoableChangeListener(VetoableChangeListener listener);
    Registration addPropertyChangeListener(PropertyChangeListener listener);
}
