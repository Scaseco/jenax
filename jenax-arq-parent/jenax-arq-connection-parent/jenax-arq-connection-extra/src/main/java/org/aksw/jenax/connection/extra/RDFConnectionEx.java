package org.aksw.jenax.connection.extra;

import org.apache.jena.rdfconnection.RDFConnection;

public interface RDFConnectionEx
	extends RDFConnection
{
	RDFConnectionMetaData getMetaData();
}
