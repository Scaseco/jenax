package org.aksw.facete.v4.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.aksw.jenax.arq.datashape.viewselector.EntityClassifier;
import org.aksw.jenax.arq.util.node.NodeTransformLib2;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.Fragment2Impl;
import org.aksw.jenax.sparql.fragment.impl.Fragment3Impl;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.topbraid.shacl.model.SHFactory;

/**
 * Class for resolving paths of RDF properties (via {@link FacetPath}} to SPARQL elements.
 */
public class PropertyResolverImpl
    implements PropertyResolver
{
    public static final Property virtualPropertyDefinition = ResourceFactory.createProperty("https://w3id.org/aksw/norse#sparqlElement");

    public static Dataset getVirtualProperties() {
        if (virtualProperties == null) {
            synchronized (PropertyResolverImpl.class) {
                if (virtualProperties == null) {
                    virtualProperties = RDFDataMgr.loadDataset("virtual-properties.ttl");
                }
            }
        }
        return virtualProperties;
    }

    // FIXME This must be made configurable
    private static Dataset virtualProperties = null;

//    static {
//        loadShacl();
//    }
    // public static Map<String, BinaryRelation> userDefinedProperties = new HashMap<>();

    @Override
    public Fragment resolve(Node property) {
        Fragment result = null;
        if (NodeUtils.ANY_IRI.equals(property)) {
            result = new Fragment3Impl(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o), Vars.s, Vars.p, Vars.o);
        }

        if (result == null && property.isURI()) {
            String str = property.getURI();

            String fnPrefix = "fn:";
            if (str.startsWith(fnPrefix)) {
                String fnIri = str.substring(fnPrefix.length());
                Element elt = new ElementBind(Vars.o, new E_Function(fnIri, new ExprList(new ExprVar(Vars.s))));
                result = new Fragment2Impl(elt, Vars.s, Vars.o);
            }
        }

        if (result == null) {
            Fragment2 relation = null;
            Model model = getVirtualProperties().getDefaultModel();
            RDFNode rdfNode = model.asRDFNode(property);
            if (rdfNode != null && rdfNode.isResource()) {
                Resource r = rdfNode.asResource();
                String definition = Optional.ofNullable(r.getProperty(virtualPropertyDefinition)).map(Statement::getString).orElse(null);
                if (definition != null) {
                    Query query = QueryFactory.create(definition);
                    relation = FragmentUtils.fromQuery(query).toFragment2();

//                if (relation == null) {
//                    relation = userDefinedProperties.get(str);
//                }

                    result = relation.toFragment2();
//                        Map<Node, Node> remap = new HashMap<>();
//                        remap.put(br.getSourceVar(), parentVar);
//                        remap.put(br.getTargetVar(), targetVar);
//                        br = br.applyNodeTransform(NodeTransformLib2.wrapWithNullAsIdentity(remap::get));
//                        result = br.getElement();
//
//                        result = ElementUtils.flatten(result);

                    Element elt = result.getElement();
                    if (elt instanceof ElementSubQuery) {
                        elt = ((ElementSubQuery)result).getQuery().getQueryPattern();
                        result = new Fragment2Impl(elt, relation.getSourceVar(), relation.getTargetVar());
                    }
                }
            }
        }

        if (result == null) {
            result = Fragment2Impl.create(property);
        }
        return result;
                // resolve(Vars.s, property, Vars.o, true);
    }

    // XXX Use something nicer than incremented ids
    protected int nextPropertyId = 0;
    protected Map<String, Fragment2> iriToRelation = new LinkedHashMap<>();

    public void put(String iri, Fragment2 relation) {
        iriToRelation.put(iri, relation);
    }

    public Element resolve(Var parentVar, Node predicateNode, Var targetVar, boolean isFwd) {
        return createElementFromConcretePredicate(parentVar, predicateNode, targetVar, isFwd);
    }


    public static void testLoadShacl() {
        SHFactory.ensureInited();
        Model shaclModel = RDFDataMgr.loadModel("test.r2rml.core.shacl.ttl");
        // List<ShNodeShape> nodeShapes = shaclModel.listResourcesWithProperty(SH.property).mapWith(r -> r.as(ShNodeShape.class)).toList();
        EntityClassifier entityClassifier = new EntityClassifier(Vars.s);

        EntityClassifier.registerNodeShapes(entityClassifier, shaclModel);


//        Model model = ModelFactory.createModelForGraph(graph);
//        SparqlQueryConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(model));
//
//        Query concept = QueryFactory.create("SELECT DISTINCT ?s { ?s ?p ?o }");
//
//        boolean materialize = false;
//        if (materialize) {
//            Query c = concept;
//            Table table = QueryExecutionUtils.execSelectTable(() -> QueryExecutionFactory.create(c, model));
//
//            Query tmp = QueryFactory.create("SELECT DISTINCT ?s {}");
//            tmp.setQueryPattern(new ElementData(table.getVars(), Lists.newArrayList(table.rows())));
//            concept = tmp;
//        }
//
//
//        // raw.setValuesDataBlock(table.getVars(), Lists.newArrayList(table.rows()));
//        EntityBaseQuery ebq = new EntityBaseQuery(Collections.singletonList(Vars.s), new EntityTemplateImpl(), concept);
//
//        Expr partitionSortExpr = new ExprAggregator(Var.alloc("dummy"),
//                new AggMin(new E_Str(new ExprVar(Vars.o))));
//        ebq.getPartitionOrderBy().add(new SortCondition(partitionSortExpr, Query.ORDER_ASCENDING));
//
////        ebq.getStandardQuery().setOffset(5);
////        ebq.getStandardQuery().setLimit(3);
//
//
//        EntityQueryImpl eq = new EntityQueryImpl();
//        eq.setBaseQuery(ebq);
//        eq.getMandatoryJoins().add(new GraphPartitionJoin(entityGraphFragment));
//
//        EntityQueryBasic basic = EntityQueryRx.assembleEntityAndAttributeParts(eq);
//        System.out.println("Entity Query: " + basic);
//
//        EntityQueryRx.execConstructEntitiesNg(conn::query, basic).forEach(quad -> System.out.println(quad));


        Fragment r = entityClassifier.createClassifyingRelation();

        Query query = r.toQuery();
        getVirtualProperties().getDefaultModel().createResource("urn:shaclShape").addProperty(virtualPropertyDefinition, query.toString());
//        userDefinedProperties.put("urn:shaclShape", r.toBinaryRelation());
//
//        UnaryRelation testConcept = ConceptUtils.createForRdfType("http://foo.bar/baz");
//        Relation s = testConcept.join().with(r, r.getVars().get(0)); //r.joinOn(r.getVars().get(0)).with(testConcept);
//
//
//        Relation grouped =  RelationUtils.groupBy(s, s.getVars().iterator().next(), Vars.c, false);
//        System.out.println("Grouped relation: " + grouped);
//
//        Op op = Algebra.optimize(Algebra.compile(grouped.getElement()));
//        System.out.println(op);
    }

    // FIXME Use a separate class for allocation?
    // We could actually maintain an internal SHACL model
    /** Allocate a fresh property name for a given SPARQL Property path */
    public String allocate(Path path) {
        Fragment2 relation = Fragment2Impl.create(path);
        String iri = "urn:x-jenax:custom-property-" + nextPropertyId++;
        put(iri, relation);
        return iri;
    }

    public static Element createElementFromConcretePredicate(Var parentVar, Node predicateNode, Var targetVar, boolean isFwd) {
        Element result = null;
        if (predicateNode.isURI()) {
            String str = predicateNode.getURI();

            String fnPrefix = "fn:";
            if (str.startsWith(fnPrefix)) {
                String fnIri = str.substring(fnPrefix.length());
                result = new ElementBind(targetVar, new E_Function(fnIri, new ExprList(new ExprVar(parentVar))));
            }

            if (result == null) {
                Fragment2 relation = null;
                Model model = getVirtualProperties().getDefaultModel();
                RDFNode rdfNode = model.asRDFNode(predicateNode);
                if (rdfNode != null && rdfNode.isResource()) {
                    Resource r = rdfNode.asResource();
                    String definition = Optional.ofNullable(r.getProperty(virtualPropertyDefinition)).map(Statement::getString).orElse(null);
                    if (definition != null) {
                        Query query = QueryFactory.create(definition);
                        relation = FragmentUtils.fromQuery(query).toFragment2();

    //                if (relation == null) {
    //                    relation = userDefinedProperties.get(str);
    //                }

                        Fragment2 br = relation.toFragment2();
                        Map<Node, Node> remap = new HashMap<>();
                        remap.put(br.getSourceVar(), parentVar);
                        remap.put(br.getTargetVar(), targetVar);
                        br = br.applyNodeTransform(NodeTransformLib2.wrapWithNullAsIdentity(remap::get));
                        result = br.getElement();

                        result = ElementUtils.flatten(result);

                        if (result instanceof ElementSubQuery) {
                            result = ((ElementSubQuery)result).getQuery().getQueryPattern();
                        }
                    }
                }
            }
        }

        if (result == null) {
            result = ElementUtils.createElementTriple(parentVar, predicateNode, targetVar, isFwd);
        }
        return result;
    }
}
