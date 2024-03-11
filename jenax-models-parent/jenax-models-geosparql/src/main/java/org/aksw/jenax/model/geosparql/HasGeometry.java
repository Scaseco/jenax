package org.aksw.jenax.model.geosparql;

import java.util.Set;

import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.geosparql.implementation.vocabulary.GeoSPARQL_URI;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface HasGeometry
    extends Resource
{
    @Iri(GeoSPARQL_URI.GEO_URI + "hasGeometry")
    Set<Geometry> getHasGeometry();

    default HasGeometry addGeometry(Resource geometry) {
        getHasGeometry().add(geometry.as(Geometry.class));
        return this;
    }

    default Geometry addNewGeometry() {
        Geometry result = getModel().createResource().as(Geometry.class);
        addGeometry(result);
        return result;
    }
}
