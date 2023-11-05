package org.aksw.jenax.dataaccess.sparql.connection.common;

import java.util.function.Function;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RDFConnectionTransform
    extends Function<RDFConnection, RDFConnection>
{
}
