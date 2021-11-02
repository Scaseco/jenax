package org.aksw.jenax.connection.extra;

import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionWrapper;

/**
 * Extends an RDFConnection with a description attribute for metadata
 * 
 * @author raven
 *
 */
public class RDFConnectionExImpl
	extends RDFConnectionWrapper
	implements RDFConnectionEx
{
	protected RDFConnectionMetaData metadata;
	
	public RDFConnectionExImpl(RDFConnection other, RDFConnectionMetaData metadata) {
		super(other);
		this.metadata = metadata;
	}

	public RDFConnectionMetaData getMetaData() {
		return metadata;
	}
}
