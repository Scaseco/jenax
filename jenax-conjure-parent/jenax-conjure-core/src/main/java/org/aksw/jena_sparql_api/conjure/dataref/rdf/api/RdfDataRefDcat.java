package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefDcat;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.RdfTypeNs;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;


@ResourceView
@RdfTypeNs("rpif")
public interface RdfDataRefDcat
    extends DataRefDcat, RdfDataRef
{
//    @IriNs("rpif")
//    // @PolymorphicOnly
//    Resource getDcatRecord();
//    DataRefDcat setDcatRecord(Resource dcatRecord);

    @Iri("rpif:dcatRecord")
    @HashId
    @IriType
    Node getDcatRecordNode();
    RdfDataRefDcat setDcatRecordNode(Node iri);

    @Override
    default <T> T acceptRdf(RdfDataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    public static RdfDataRefDcat create(Model model, Resource dcatRecord) {
        model.add(dcatRecord.getModel());
        dcatRecord = dcatRecord.inModel(model);

        RdfDataRefDcat result = model.createResource().as(RdfDataRefDcat.class)
                .setDcatRecordNode(dcatRecord.asNode());

        return result;
    }
}
