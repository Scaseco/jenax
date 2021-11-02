package org.aksw.facete.v3.api.path;

import org.apache.jena.sparql.syntax.Element;

public interface Triplet {
	Containlet getContainlet();
	Nodelet getS();
	Nodelet getP();
	Nodelet getO();
	
	//Element getEffectiveElement();
}
