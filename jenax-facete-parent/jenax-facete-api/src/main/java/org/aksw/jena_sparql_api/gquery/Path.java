package org.aksw.jena_sparql_api.gquery;

import java.util.Collection;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;

public interface Path
	extends Expression
{
	Path fwd(String p);
	Path fwd(Node p);
	Path fwd(Resource p);	
	Path fwdVia(String alias);
	Path fwdVia(Var alias);
	Collection<Path> fwds();
	
	Path bwd(String p);
	Path bwd(Node p);
	Path bwd(Resource p);
	Path bwdVia(String alias);
	Path bwdVia(Var alias);
	Collection<Path> bwds();

	/**
	 * ?s ?p ?o
	 * FILTER(?p IN (rdfs:label, dct:title))
	 * 
	 * root.as(Vars.s)
	 *   .via(Vars.p).filter(fb -> fb.in(RDFS.label, DCT.title)).end()
	 * .as(Vars.o)
	 *   
	 *   
	 * 
	 * @return
	 */
	Path via(String alias);
	Path via(Var var);
	
	Path step(String p, boolean isFwd);
	Path step(Node p, boolean isFwd);
	Path step(Resource p, boolean isFwd);
	Path stepVia(String alias, boolean isFwd);
	Path stepVia(Var alias, boolean isFwd);
	Collection<Path> steps();
	
	Path as(String name);
	Path as(Var var);
}
