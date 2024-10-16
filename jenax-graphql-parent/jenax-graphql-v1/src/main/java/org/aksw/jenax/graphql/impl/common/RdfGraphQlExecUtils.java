package org.aksw.jenax.graphql.impl.common;

import java.util.stream.Stream;

import org.aksw.jenax.graphql.rdf.api.RdfGraphQlDataProvider;
import org.aksw.jenax.graphql.rdf.api.RdfGraphQlExec;
import org.aksw.jenax.ron.RdfArray;
import org.aksw.jenax.ron.RdfArrayImpl;
import org.aksw.jenax.ron.RdfElement;
import org.aksw.jenax.ron.RdfObject;
import org.aksw.jenax.ron.RdfObjectImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

public class RdfGraphQlExecUtils {
    public static final Property data = ResourceFactory.createProperty("urn:data");
    public static final Property errors = ResourceFactory.createProperty("urn:errors");

    // @Override
    public static RdfObject write(RdfGraphQlExec exec) {
        // { data: { ...  dataStreams ... } }
        RdfObject dataObject = new RdfObjectImpl();

        for (RdfGraphQlDataProvider dataProvider : exec.getDataProviders()) {
            String name = dataProvider.getName();

            // TODO Handle the case of non-array responses
            RdfArray items = new RdfArrayImpl();
            try (Stream<RdfElement> stream = dataProvider.openStream()) {
                stream.forEach(items::add);
            }

            Node nameIri = NodeFactory.createURI(name);
            dataObject.addForward(nameIri, items);
        }

        RdfArray errorsArray = new RdfArrayImpl();

        RdfObject result = new RdfObjectImpl();
        result.addForward(data, dataObject);
        result.addForward(errors, errorsArray);

        // FIXME Migrate extensions to RDFElement
//        JsonObject metadata = GraphQlExecUtils.collectExtensions(exec);
//        if (!metadata.keySet().isEmpty()) {
//            result.addForward("extensions", metadata);
//        }

        return result;
    }
}
