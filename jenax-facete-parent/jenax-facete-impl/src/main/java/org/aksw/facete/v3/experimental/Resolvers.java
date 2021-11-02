package org.aksw.facete.v3.experimental;

import org.aksw.commons.jena.graph.GraphVar;
import org.aksw.commons.jena.graph.GraphVarImpl;
import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.relation.query.PartitionedQuery1;
import org.aksw.jenax.sparql.relation.query.PartitionedQuery1Impl;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.Template;

public class Resolvers {

    public static ResolverTemplate from(PartitionedQuery1 pq) {
        ResolverTemplate result = from(pq.getQuery(), pq.getPartitionVar());
        return result;
    }

    public static Resolver create() {
        PartitionedQuery1 pq = PartitionedQuery1Impl.from(QueryFactory.create("CONSTRUCT WHERE {}"), Vars.s);
        Resolver result = Resolvers.from(pq);

        return result;
    }

    public static ResolverTemplate from(Query view, Node viewVar) {
        RDFNode node = toRdfModel(view, viewVar);
        ResolverTemplate result = new ResolverTemplate(null, view, node, null, null);

        return result;
    }

    public static RDFNode toRdfModel(Query query, Node rootNode) {
        Template template = query.getConstructTemplate();
        GraphVar graphVar = new GraphVarImpl(GraphFactory.createDefaultGraph());
        GraphUtil.add(graphVar, template.getTriples());
        Model model = ModelFactory.createModelForGraph(graphVar);

        Resource root = model.getRDFNode(rootNode).asResource();

        return root;
    }
}
