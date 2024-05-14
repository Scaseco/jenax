package org.aksw.jenax.dataaccess.sparql.polyfill.detector;

import java.util.function.Predicate;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;

public interface Condition
    extends Predicate<RdfDataSource>
{
}
