package org.aksw.jena_sparql_api.gquery;

import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

// Just an idea for a property path expression builder
public class PpeBuilder {
	protected Path path;
	
	public PpeBuilder() {
		this(null);
	}

	public PpeBuilder(Path path) {
		super();
		this.path = path;
	}

//	public static PpeBuilder fwd(String p) {
//		return fwd(NodeFactory.createURI(p));
//	}
//
//	public static PpeBuilder fwd(Node p) {
//		return new PpeBuilder(PathFactory.pathLink(p));
//	}

	public PpeBuilder fwd(String p) {		
		return fwd(NodeFactory.createURI(p));
	}

	public PpeBuilder fwd(Node p) {
		return new PpeBuilder(PathFactory.pathLink(p));
	}

	public PpeBuilder fwd(Resource p) {
		return fwd(p.asNode());
	}

	public PpeBuilder zeroOrMore() {
		path = PathFactory.pathZeroOrMore1(path);
		return this;
	}
	
	@SafeVarargs
	public final PpeBuilder alt(Function<? super PpeBuilder, ? extends PpeBuilder> ... builders) {
		
		return this;
	}
	
	
	public static void main(String[] args) {
		new PpeBuilder().alt(b -> b.fwd(RDF.type), b -> b.fwd(RDF.type)).fwd(RDFS.seeAlso).zeroOrMore();
	}
}
