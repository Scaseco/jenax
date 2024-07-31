package org.aksw.jenax.graphql.sparql;

import java.util.Collection;
import java.util.Set;

import org.aksw.jenax.model.shacl.domain.ShPropertyShape;
import org.aksw.jenax.path.core.FacetPath;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.path.P_Path0;

/**
 * A resolver is used to map elements of a graphql query to corresponding elements of an RDF graph.
 * For example, it is used to map fieldNames to classes and properties.
 *
 * This interface decouples the {@link GraphQlToSparqlConverter} from specific representations of
 * metadata for a given RDF graph.
 *
 * The abstraction of this interface is not yet ideal, so consider it internal only.
 */
public interface GraphQlResolver {
    Set<Node> resolveKeyToClasses(String key);
    FacetPath resolveKeyToProperty(String key);

    Collection<ShPropertyShape> getGlobalPropertyShapes(P_Path0 path);
}
