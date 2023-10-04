package org.aksw.jenax.graphql.playground;

import org.aksw.jenax.connection.datasource.RdfDataSource;
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
                "{ Pokemon(limit: 10, offset: 5, orderBy: [{colour: desc}, {path: [colour], dir: asc}]) {"
//                + "maleRatio, colour(xid: \"red\"), speciesOf @inverse { baseHP }, "
                + "label, colour, speciesOf @inverse { label } "
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
