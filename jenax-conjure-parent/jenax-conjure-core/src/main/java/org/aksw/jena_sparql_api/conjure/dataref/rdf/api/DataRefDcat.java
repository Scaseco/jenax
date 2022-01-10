package org.aksw.jena_sparql_api.conjure.dataref.rdf.api;

import org.aksw.jena_sparql_api.conjure.dataref.core.api.PlainDataRefDcat;
import org.aksw.jena_sparql_api.mapper.annotation.HashId;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.RdfTypeNs;
import org.aksw.jena_sparql_api.mapper.annotation.ResourceView;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;


@ResourceView
@RdfTypeNs("rpif")
public interface DataRefDcat
    extends PlainDataRefDcat, DataRef
{
//    @IriNs("rpif")
//    // @PolymorphicOnly
//    Resource getDcatRecord();
//    DataRefDcat setDcatRecord(Resource dcatRecord);

    @Iri("rpif:dcatRecord")
    @HashId
    @IriType
    Node getDcatRecordNode();
    DataRefDcat setDcatRecordNode(Node iri);

    @Override
    default <T> T accept2(DataRefVisitor<T> visitor) {
        T result = visitor.visit(this);
        return result;
    }

    public static DataRefDcat create(Model model, Resource dcatRecord) {
        model.add(dcatRecord.getModel());
        dcatRecord = dcatRecord.inModel(model);

        DataRefDcat result = model.createResource().as(DataRefDcat.class)
                .setDcatRecordNode(dcatRecord.asNode());

        return result;
    }
}
