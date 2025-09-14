package org.aksw.jenax.dataaccess.sparql.creator.source;

public interface SourceSpec {
    Source getSource();
    SourceDescriptor getDescriptor();
    boolean isLenient();
}
