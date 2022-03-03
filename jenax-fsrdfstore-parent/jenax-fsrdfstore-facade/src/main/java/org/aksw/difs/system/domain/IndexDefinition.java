package org.aksw.difs.system.domain;

import org.aksw.difs.sys.vocab.common.DIFSTerms;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

/**
 * Configuration of an index. Must only be referenced by a single {@link StoreDefinition} instance.
 * 
 * @author raven
 *
 */
@ResourceView
public interface IndexDefinition
	extends Resource
{
	@Inverse
	@HashId
	StoreDefinition getStoreSpec();
	
	@Iri(DIFSTerms.predicate)
	@HashId
	Node getPredicate();
	IndexDefinition setPredicate(Node predicate);

	@Iri(DIFSTerms.folder)
	@HashId
	String getPath();
	IndexDefinition setPath(String path);

	@Iri(DIFSTerms.method)
	@HashId
	String getMethod();
	IndexDefinition setMethod(String className);
}
