package org.aksw.jenax.arq.connection.core;

import java.util.function.Function;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RDFConnectionTransform
    extends Function<RDFConnection, RDFConnection>
{
}
