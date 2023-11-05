package org.aksw.jenax.arq.util.query;

import java.util.function.Function;

import org.apache.jena.query.Query;

/**
 * Marker interface. Also makes it easier to search for implementations rather than that of Function.
 * @author raven
 *
 */
public interface QueryTransform
	extends Function<Query, Query>
{

}
