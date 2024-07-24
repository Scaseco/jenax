package org.aksw.facete.v3.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aksw.commons.collections.maps.MapFromValueConverter;
import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromNodeMapper;
import org.aksw.jena_sparql_api.rdf.collections.ConverterFromNodeMapperAndModel;
import org.aksw.jena_sparql_api.rdf.collections.NodeMappers;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.utils.views.map.MapFromKeyConverter;
import org.aksw.jena_sparql_api.utils.views.map.MapFromResource;
import org.aksw.jena_sparql_api.utils.views.map.MapVocab;
import org.aksw.jenax.arq.util.node.NodeTransformCollectNodes;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.NodeTransformExpr;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

import com.google.common.collect.Maps;
import com.google.common.collect.Streams;

public class FacetConstraintImpl
    extends ResourceImpl
    implements FacetConstraint
{
    public FacetConstraintImpl(Node n, EnhGraph m) {
        super(n, m);
    }

    @Override
    public boolean enabled() {
        return ResourceUtils.tryGetLiteralPropertyValue(this, Vocab.enabled, Boolean.class).orElse(true);
    }

    @Override
    public FacetConstraint enabled(boolean onOrOff) {

        if(onOrOff) {
            ResourceUtils.setLiteralProperty(this, Vocab.enabled, null);
        } else {
            ResourceUtils.setLiteralProperty(this, Vocab.enabled, onOrOff);
        }

        return this;
    }

    @Override
    public Expr expr() {
        String str = ResourceUtils.tryGetLiteralPropertyValue(this, Vocab.expr, String.class).orElse(null);
        Expr result = ExprUtils.parse(str);
        //result = result.applyNodeTransform(FacetConstraintImpl::varToBlankNode);

        Map<Integer, Node> decoder = getBnodeMap();

        // Substitute blank nodes in the given expressions with the ids from the mapping
        // TODO We could check for corruption - e.g. bnode label that are not mapped
        result = ExprTransformer.transform(new NodeTransformExpr(x -> {
            Integer id = tryGetBnodeAsInt(x);
            Node r = decoder.getOrDefault(id, x);
            return r;
        }), result);


        return result;
    }

    public static Integer tryGetBnodeAsInt(Node n) {
        Integer result = null;
        if(n.isVariable()) { // n.isBlank()
            String str = n.getName(); //n.getBlankNodeLabel();
            if(str.matches("\\d+")) {
                result = Integer.parseInt(str);
            }
        }
        return result;
    }

    // Mapping of bnodes in the contraint expression *string* to bnodes in the model
    public Map<Integer, Node> getBnodeMap() {
        Map<RDFNode, RDFNode> rawMap = new MapFromResource(this, Vocab.mapping, MapVocab.key, MapVocab.value);

        //Map<RDFNode, Resource> rawMap = new HashMap<>();

        Model model = getModel();

        Map<Integer, RDFNode> tmpMap = new MapFromKeyConverter<>(
                rawMap,
                new ConverterFromNodeMapperAndModel<>(model, RDFNode.class, new ConverterFromNodeMapper<>(NodeMappers.from(Integer.class))));

        Map<Integer, Node> result = new MapFromValueConverter<>(
                tmpMap, new ConverterFromNodeMapperAndModel<>(model, RDFNode.class, new ConverterFromNodeMapper<>(NodeMappers.PASSTHROUGH)));

        return result;
    }

    public static Node varToBlankNode(Node node) {
        return Optional.of(node)
                .filter(Node::isVariable)
                .map(y -> (Var)y)
                .map(Var::getName)
                .filter(n -> n.startsWith("___"))
                .map(n -> n.substring(3).replace("_", "-"))
                .map(NodeFactory::createBlankNode)
                .orElse(node);
    }


    public static Node blankNodeToVar(Node node) {
        return !node.isBlank() ? node : Var.alloc("___" + node.getBlankNodeLabel().replace("-", "_"));
    }

    /**
     * Applies an expression-based constraint to the given facet node
     *
     *
     *
     */
    @Override
    public FacetConstraint expr(Expr expr) {
        // Collect the blank nodes referenecd in the expression
        NodeTransformCollectNodes collector = new NodeTransformCollectNodes();
        Set<Node> nodes = collector.getNodes();

        ExprTransformer.transform(new NodeTransformExpr(collector), expr);

        // Create a map from blank node
        Map<Integer, Node> bnodeToInt = Streams.zip(
            nodes.stream().filter(Node::isBlank),
            IntStream.range(0, Integer.MAX_VALUE).boxed(),
            (v, i) -> Maps.immutableEntry(i, v)
        ).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        Map<Node, Node> encoder = bnodeToInt.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getValue,
                        e -> Var.alloc(Objects.toString(e.getKey()))));

//		for(Node node : nodes) {
//			System.out.println("  " + node + " -> " + encoder.get(node));
//		}

        // Substitute blank nodes in the given expressions with the ids from the mapping
        Expr effectiveExpr = ExprTransformer.transform(new NodeTransformExpr(x -> encoder.getOrDefault(x, x)), expr);

        // Append the mapping to the resource
        Map<Integer, Node> mapView = getBnodeMap();
        mapView.clear();

        mapView.putAll(bnodeToInt);



//		Resource mapping = ResourceUtils.getPropertyValue(this, Vocab.mapping, null);
//		if(mapping == null) {
//			mapping = this.getModel().createResource();
//			ResourceUtils.setProperty(this,  Vocab.mapping, mapping);
//		}



        //expr = expr.applyNodeTransform(FacetConstraintImpl::blankNodeToVar);
//		expr = ExprTransformer.transform(new NodeTransformExpr(FacetConstraintImpl::blankNodeToVar), expr);



        String str = ExprUtils.fmtSPARQL(effectiveExpr);
        ResourceUtils.setLiteralProperty(this, Vocab.expr, str);
//		RDFDataMgr.write(System.err, this.getModel(), RDFFormat.TURTLE_PRETTY);

        return this;
    }



//	@Override
    public FacetConstraint exprOld(Expr expr) {
        //expr = expr.applyNodeTransform(FacetConstraintImpl::blankNodeToVar);
        expr = ExprTransformer.transform(new NodeTransformExpr(FacetConstraintImpl::blankNodeToVar), expr);



        String str = ExprUtils.fmtSPARQL(expr);
        ResourceUtils.setLiteralProperty(this, Vocab.expr, str);

        return this;
    }


    @Override
    public String toString() {
        // Substitute references in the expression with their respective toString representation
        Expr expr = expr();

        Map<Node, BgpNode> map = HLFacetConstraintImpl.mentionedBgpNodes(this.getModel(), expr);
        Expr e = org.aksw.jenax.arq.util.expr.ExprUtils.applyNodeTransform(expr, n -> {
            Node r;
            BgpNode fn = map.get(n);
            if(fn != null) {
                r = NodeFactory.createLiteral("[" + fn + "]");
            } else {
                r = n;
            }

            return r;
        });

        //String result = Objects.toString(e);
        String result = org.apache.jena.sparql.util.ExprUtils.fmtSPARQL(e);
        return  result;
    }


    public static void main(String[] args) {
        Model m = ModelFactory.createDefaultModel();

        Resource b1 = m.createResource();
        Resource b2 = m.createResource();
        Resource b3 = m.createResource();

        b1.addProperty(RDF.type, RDFS.Resource);
        b2.addProperty(RDF.type, RDFS.Resource);
        b3.addProperty(RDF.type, RDFS.Resource);

        Resource iri = m.createResource("http://www.example.org/test");

        Expr e1 = new E_LogicalOr(NodeValue.makeNode(b1.asNode()), new E_LogicalOr(NodeValue.makeNode(b2.asNode()), NodeValue.makeNode(iri.asNode())));

        FacetConstraint c = m.createResource().as(FacetConstraint.class);
        c.expr(e1);

        Expr e2 = NodeValue.makeNode(b3.asNode());
        c.expr(e2);



        RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_FLAT);

    }

    @Override
    public void unlink() {
        // Anything to do?
    }
}
