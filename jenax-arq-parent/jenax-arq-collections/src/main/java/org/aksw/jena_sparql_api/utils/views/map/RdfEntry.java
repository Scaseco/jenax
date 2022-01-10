package org.aksw.jena_sparql_api.utils.views.map;

import java.util.Map.Entry;

import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * Interface for RDF-backed entry representations
 *
 * @author raven
 *
 */
public interface RdfEntry<K extends RDFNode, V extends RDFNode>
    extends Resource, Entry<K, V>
{
    @Override
    RdfEntry<K, V> inModel(Model m);

    // The property that links the owner to this entry
    Property getOwnerProperty();

    default Resource getOwner() {
        Property ownerProperty = getOwnerProperty();
        Resource result = ResourceUtils.getReversePropertyValue(this, ownerProperty);
        return result;
    }
}
