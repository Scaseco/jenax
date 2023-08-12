package org.aksw.jenax.arq.dataset.api;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Quad;

/**
 * Evolving.
 *
 * Interface for dataset implementations that only allow for a single named graph.
 * Typical use is for event streams where each event is encoded as a named graph.
 *
 * Whether a default graph may exist instead of or in addition to a named graph
 * is (currently) unspecified. It is recommended to only use this class for
 * named graphs.
 *
 * @author raven
 *
 */
public interface DatasetOneNg
    extends Dataset
{
    String getGraphName();

    default Model getModel() {
    	// TODO Return a model view over this rather than exposing the internal model
        String g = getGraphName();
        Model result = Quad.isDefaultGraph(NodeFactory.createURI(g))
            ? getDefaultModel()
            : getNamedModel(g);

        return result;
    }
    
    /** Return a resource in this dataset's only graph which also has the same name as the graph */ 
    default Resource getSelfResource() {
    	String g = getGraphName();
    	return getModel().createResource(g);
    }
}
