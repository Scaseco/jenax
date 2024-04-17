package org.aksw.jenax.model.geosparql;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Inverse;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface Geometry
    extends Resource
{
    // Typically there is just "feature"-resource that links to a "geometry"-resource
    // via geo:hasGeometry
    @HashId
    @Iri(GeoSPARQL_URI.GEO_URI + "hasGeometry")
    @Inverse
    Set<HasGeometry> getOwners();

    @Iri(GeoSPARQL_URI.GEO_URI + "asWKT")
    @HashId
    Node getAsWKT();
    Geometry setAsWKT(Node wkt);

    default Geometry setAsWKT(Resource wkt) {
        return setAsWKT(wkt.asNode());
    }


    // TODO GeoJSON, ...
}
