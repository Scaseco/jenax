package org.aksw.jena_sparql_api.schema;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.vocabulary.SH;

/**
 * A helper view for classes for which shacl NodeShape 'annotations' exist
 * and as such these classes appear as values of sh:targetClass properties.
 * Eases navigation to the set of related node shapes
 *
 * @author raven
 *
 */
@ResourceView
public interface SHAnnotatedClass
    extends Resource
{
    @Inverse
    @Iri(SH.NS + "targetClass")
    Set<NodeSchemaFromNodeShape> getNodeShapes();
}
