package org.aksw.jenax.graphql;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jenax.facete.treequery2.api.NodeQuery;
import org.aksw.jenax.facete.treequery2.impl.NodeQueryImpl;
import org.aksw.jenax.model.voidx.api.VoidDataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.VOID;

import graphql.language.Argument;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.Node;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.parser.Parser;


public class MainPlaygroundGraphql {




    public static void main(String[] args) {
        Model dataModel = RDFDataMgr.loadModel("/home/raven/Datasets/pokedex/pokedex-data-rdf.ttl");
        // SparqlStmtMgr.execSparql(model, "void/sportal/compact/qb2.rq");

        // TODO Generate basic void

        Model voidModel = RDFDataMgr.loadModel("/home/raven/Datasets/pokedex/pokedex.void.ttl");
        Resource voidDatasetRaw = IterableUtils.expectOneItem(voidModel.listSubjectsWithProperty(RDF.type, VOID.Dataset).toList());

        VoidDataset voidDataset = voidDatasetRaw.as(VoidDataset.class);

        System.out.println(voidDataset.getClassPartitionMap().keySet());
        System.out.println(voidDataset.getPropertyPartitionMap().keySet());

        Parser parser = new Parser();
        Document document = parser.parseDocument("{ items(type: \"Pokemon\") {  Instrument(id: \"1234\") {    Reference {      Name    }  }} }");
        System.out.println(document.toString());

        NodeQuery nodeQuery = NodeQueryImpl.newRoot();

        graphQlToNodeQuery(document, nodeQuery);

    }

    public static Object graphQlToNodeQuery(Node<?> node, NodeQuery nodeQuery) {

        if (node instanceof OperationDefinition) {
            OperationDefinition queryNode = (OperationDefinition)node;
            SelectionSet selectionSet = queryNode.getSelectionSet();
            //selectionSet.
            System.out.println(selectionSet);
            for (Selection<?> selection : selectionSet.getSelections()) {
                if (selection instanceof Field) {
                    Field field = (Field)selection;
                    for (Argument arg : field.getArguments()) {
                        String k = arg.getName();



                        Value<?> rawV = arg.getValue();
                        if (rawV instanceof StringValue) {
                            StringValue v = (StringValue)rawV;
                            String str = v.getValue();
                        }


                    }
                    System.out.println(field);
                    // field.get
                    // System.out.println(field.);
                }
                // System.out.println(selection);
                // selection.fi
            }
        } else {
            for (Node child : node.getChildren()) {
                graphQlToNodeQuery(child, nodeQuery);
            }
        }


        // System.out.println(queryNode.getSelectionSet());

        // System.out.println(queryNode);
        // return checkDepthLimit(queryNode.get());

//        for (Entry<String, List<Node>> entry : doc.getNamedChildren().getChildren().entrySet()) {
//System.out.println(entry);
//        }

//        for (Definition def : doc.getDefinitions()) {
//            System.out.println(def);
//
//            for (Node child : def.getChildren()) {
//                // child.get
//                // graphQlToNodeQuery(child);
//            }
//        }
//        for (Node child : doc.getChildren()) {
//
//            System.out.println(child);
//
//        }
        // System.out.println(doc.getChildren());

        // RelationQue
        return null;
    }
}
