package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefUrl;
import org.aksw.jenax.annotation.reprogen.RdfType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.rdf.model.ModelFactory;

@ResourceView
@RdfType("rpif:DataRefEmpty")
public interface RdfDataRefEmpty
    extends DataRefUrl, RdfDataRef
{
    @Override
    default <T> T acceptRdf(RdfDataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    public static RdfDataRefEmpty create() {
        RdfDataRefEmpty result = ModelFactory.createDefaultModel().createResource().as(RdfDataRefEmpty.class);
        return result;
    }
}
