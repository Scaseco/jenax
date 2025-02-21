package org.aksw.jenax.dataaccess.sparql.polyfill.detector;

import java.util.function.Predicate;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;

public interface Condition
    extends Predicate<RDFDataSource>
{
}
