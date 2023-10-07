package org.aksw.jenax.graphql.playground;

import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.graphql.GraphQlExecFactory;
import org.aksw.jenax.graphql.impl.core.GraphQlExecUtils;
import org.aksw.jenax.graphql.impl.sparql.GraphQlExecFactoryOverSparql;
import org.apache.jena.rdfconnection.RDFConnectionRemote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class MainPlaygroundGraphQl {
    public static void main(String[] args) {
        String queryStr =
                "{ Pokemon(limit: 10, offset: 5, orderBy: [{colour: desc}, {path: [colour], dir: asc}]) @rdf(base: \"http://coypu\", namespaces: { rdfs: \"http://www.w3.org/2000/01/rdf-schema#\" }) {"
//                + "maleRatio, colour(xid: \"red\"), speciesOf @inverse { baseHP }, "
                + "label @rdf(ns: rdfs), colour @rdf(iri: \"http://myvocab.org/farbe\"), speciesOf @inverse { label } "
                + "} }";


        System.out.println(queryStr);

        RdfDataSource dataSource = () -> RDFConnectionRemote.newBuilder()
                .queryEndpoint("http://localhost:8642/sparql").build();

        GraphQlExecFactory gef = GraphQlExecFactoryOverSparql.autoConfigure(dataSource);
        JsonObject jo = GraphQlExecUtils.materialize(gef, queryStr);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        System.out.println(gson.toJson(jo));
    }
}
