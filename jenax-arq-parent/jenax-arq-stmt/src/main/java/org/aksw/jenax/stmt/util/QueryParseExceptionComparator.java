package org.aksw.jenax.stmt.util;

import java.util.Comparator;

import org.apache.jena.query.QueryParseException;

/**
 * Compares QueryParseExceptions by their line and column number.
 * If multiple parsers attempt to parse a sparql statement,
 * this comparator can be used to detect which parser came farthest.
 *
 * @author raven
 *
 */
public class QueryParseExceptionComparator
    implements Comparator<QueryParseException>
{

    @Override
    public int compare(QueryParseException a, QueryParseException b) {
        int result = QueryParseExceptionUtils.doCompare(a, b);
        return result;
    }
}