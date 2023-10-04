package org.aksw.jenax.graphql.playground;

import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.graphql.GraphQlExec;
import org.aksw.jenax.graphql.GraphQlExecFactory;
import org.aksw.jenax.graphql.impl.core.GraphQlExecUtils;
import org.aksw.jenax.graphql.impl.sparql.GraphQlExecFactoryOverSparql;
import org.apache.jena.rdfconnection.RDFConnectionRemote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import graphql.language.Document;
import graphql.language.NodeChildrenContainer;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.parser.Parser;

public class MainPlaygroundGraphQl {
    public static void main(String[] args) {
        RdfDataSource dataSource = () -> RDFConnectionRemote.newBuilder()
                .queryEndpoint("http://localhost:8642/sparql").build();

        GraphQlExecFactory gef = GraphQlExecFactoryOverSparql.autoConfigure(dataSource);

        Parser parser = new Parser();
        Document document = parser.parseDocument(
              "{ Pokemon(limit: 10, offset: 5, orderBy: [{colour: desc}, {path: [colour], dir: asc}]) {"
//              + "maleRatio, colour(xid: \"red\"), speciesOf @inverse { baseHP }, "
              + "label, colour, speciesOf @inverse { label }, "
              + "} }");

        // SelectionSet selectionSet = document.getFirstDefinitionOfType(SelectionSet.class).orElse(null);
        // SelectionSet selectionSet = document.getNamedChildren().getChildOrNull("selectionSet");
        // System.out.println(selectionSet);
        OperationDefinition op = document.getFirstDefinitionOfType(OperationDefinition.class).orElse(null);
        SelectionSet ss = op.getSelectionSet();
        System.out.println(ss);

//
//
//        NodeChildrenContainer ns = document.getNamedChildren();
//        System.out.println(ns.getChildren("selectionSet"));
//        System.out.println(ns);
//        System.out.println(document);
//
        GraphQlExec ge = gef.create(document);


        // JsonObject json = ge.materialize();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject jo = GraphQlExecUtils.materialize(ge);
        System.out.println(gson.toJson(jo));
    }
}
