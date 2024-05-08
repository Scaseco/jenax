package org.aksw.jenax.dataaccess.sparql.polyfill.detector;

import java.util.List;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.Suggestion;

/**
 * A detector runs one or more queries against the given data source and may suggest one or more fixes for the broken or missing features it detects.
 */
public interface SparqlPolyfillDetector {
    String getName();
    List<Suggestion<String>> detect(RdfDataSource dataSource);
}
