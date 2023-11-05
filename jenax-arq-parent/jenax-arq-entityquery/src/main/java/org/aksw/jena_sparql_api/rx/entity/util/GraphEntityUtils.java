package org.aksw.jena_sparql_api.rx.entity.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.collections.IterableUtils;
import org.aksw.jena_sparql_api.rdf.collections.ListFromRDFList;
import org.aksw.jenax.arq.dataset.api.DatasetOneNg;
import org.aksw.jenax.arq.dataset.impl.DatasetOneNgImpl;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.triple.GraphVarImpl;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

public class GraphEntityUtils {

    /**
     * Derive a new dataset with nodes substituted with strings derived from their compositeIds
     * TODO Not implemented yet
     *
     * @param dataset
     * @return
     */
    public static Dataset resolveIds(Dataset dataset) {
        Dataset result = DatasetFactory.create();
        result.asDatasetGraph().find(Node.ANY, Node.ANY, IdVocab.compositeId.asNode(), Node.ANY);
        return null;
    }

    public static LocalDateTime createTimestamp(ZoneOffset zoneOffset) {
        Instant now = Instant.now();
        return LocalDateTime.ofEpochSecond(now.getEpochSecond(), now.getNano(), zoneOffset);
    }

    public static String createUtcGraphTimestamp() {
        LocalDateTime ldt = createTimestamp(ZoneOffset.UTC);
        return "#" + ldt.toString();
    }

    public static DatasetOneNg getOrCreateModel(Dataset dataset, RDFNode ... rdfNodes) {
        return getOrCreateModel(dataset, Arrays.asList(rdfNodes).stream().map(RDFNode::asNode).collect(Collectors.toList()));
    }

    public static DatasetOneNg getOrCreateModel(Dataset dataset, Node ... nodes) {
        return getOrCreateModel(dataset, Arrays.asList(nodes));
    }

    public static DatasetOneNg getOrCreateModel(Dataset dataset, List<Node> nodes) {
        return getOrCreateModel(dataset, nodes, GraphEntityUtils::createUtcGraphTimestamp);
    }


    public static List<Node> findEntities(Dataset dataset, List<Node> nodes, boolean isOpen) {
        Fragment1 ur = createRelationForCompositeId(nodes, isOpen);
        // Wrap the relation with a GRAPH ?s { ... } element
        ur = new Concept(new ElementNamedGraph(ur.getVar(), ur.getElement()), ur.getVar());
        Query query = ur.asQuery();

        List<Node> result = SparqlRx.execConcept(() -> QueryExecutionFactory.create(query, dataset), ur.getVar()).map(RDFNode::asNode).toList().blockingGet();
        return result;
    }

    public static Node findEntity(Dataset dataset, List<Node> nodes) {
        try {
            return IterableUtils.expectZeroOrOneItems(findEntities(dataset, nodes, false));
        } catch (Exception e) {
            throw new RuntimeException("Lookup failed because entity was not unqiue - key: " + nodes, e);
        }
    }

    // Many uses cases also require the DatasetOneNg variant which provides getSelfResource()
//	public static Resource getOrCreateSelfResource(Dataset dataset, List<Node> nodes) {
//		Resource result = Optional.of(getOrCreateModel(dataset, nodes)).map(DatasetOneNg::getSelfResource).get();
//		return result;
//	}

    public static DatasetOneNg getOrCreateModel(Dataset dataset, String ... strings) {
        List<Node> nodes = NodeUtils.createLiteralNodes(Arrays.asList(strings));
        DatasetOneNg result = getOrCreateModel(dataset, nodes);
        return result;
    }

    public static Resource getSelfResource(Dataset dataset, List<Node> nodes) {
        Resource result = Optional.ofNullable(getModel(dataset, nodes)).map(DatasetOneNg::getSelfResource).orElse(null);
        return result;
    }

    public static DatasetOneNg getModel(Dataset dataset, List<Node> nodes) {
        Node match = findEntity(dataset, nodes);
        DatasetOneNg r = match == null ? null : DatasetOneNgImpl.create(dataset, match);
        return r;
    }

    public static DatasetOneNg newModelByTimestamp(Dataset dataset) {
        String graphName = createUtcGraphTimestamp();
        Node node = NodeFactory.createURI(graphName.toString());
        DatasetOneNg r = DatasetOneNgImpl.create(dataset, node);
        return r;
    }

    public static DatasetOneNg getOrCreateModel(Dataset dataset, List<Node> nodes, Supplier<String> graphNameFactory) {

        // Note that the code below could be changed such that the txn gets promoted from read to write as necessary
        // For now we just lock as write
        Node match = findEntity(dataset, nodes);

        if (match == null) {
//			result[0] = dataset.getNamedModel(match.getURI());
//		} else {
            String newGraphName = graphNameFactory.get();
            match = NodeFactory.createURI(newGraphName);
            Resource s = dataset.getNamedModel(newGraphName).createResource(newGraphName);
            ListFromRDFList.create(s, IdVocab.compositeId).asNodes().addAll(nodes);
        }

        DatasetOneNg r = DatasetOneNgImpl.create(dataset, match);
        return r;
    }

    public static Fragment1 createRelationForEntity(List<Node> nodes, boolean isOpen) {
        Fragment1 ur = createRelationForCompositeId(nodes, isOpen);
        // Wrap the relation with a GRAPH ?s { ... } element
        ur = new Concept(new ElementNamedGraph(ur.getVar(), ur.getElement()), ur.getVar());

        return ur;
    }

    public static Fragment1 createRelationForCompositeId(List<Node> nodes, boolean open) {

        // GraphVarImpl treats variables as constant rdf nodes
        Model m = ModelFactory.createModelForGraph(new GraphVarImpl());
        Var v = Vars.g;
        Resource r = m.wrapAsResource(v);
        ListFromRDFList.create(r, IdVocab.compositeId).asNodes().addAll(nodes);

        if (open) {
            m.removeAll(null, RDF.rest, RDF.nil);
        }

        NodeTransform subst = n -> n.isBlank() ? Var.alloc(n.getBlankNodeLabel()) : n;

        Fragment1 result = new Concept(
                ElementUtils.createElementTriple(m.getGraph().find()
                        .mapWith(t -> NodeTransformLib.transform(subst, t))
                        .toList()),
                v);

        return result;
    }


    public static void main(String[] args) {
        List<Node> id1 = Arrays.asList(RDF.Nodes.type, RDFS.Nodes.label);
        List<Node> id2 = Arrays.asList(RDF.Nodes.type, RDFS.Nodes.comment);

        System.out.println(createRelationForCompositeId(id1, false).asQuery());

        Dataset ds = DatasetFactory.create();
        getOrCreateModel(ds, id1).getModel().add(RDF.type, RDF.type, RDF.type);

        RDFDataMgr.write(System.out, ds, RDFFormat.TRIG);

        getOrCreateModel(ds, id1).getModel().add(RDF.type, RDF.type, RDF.type);


        getOrCreateModel(ds, id2).getModel().add(RDFS.comment, RDFS.comment, RDFS.comment);

        RDFDataMgr.write(System.out, ds, RDFFormat.TRIG);
    }
}
