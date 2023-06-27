package org.aksw.jenax.arq.datashape.viewselector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.aksw.jena_sparql_api.rx.entity.engine.EntityQueryRx;
import org.aksw.jena_sparql_api.rx.entity.model.EntityBaseQuery;
import org.aksw.jena_sparql_api.rx.entity.model.EntityGraphFragment;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryBasic;
import org.aksw.jena_sparql_api.rx.entity.model.EntityQueryImpl;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplate;
import org.aksw.jena_sparql_api.rx.entity.model.EntityTemplateImpl;
import org.aksw.jena_sparql_api.rx.entity.model.GraphPartitionJoin;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.model.shacl.domain.HasPrefixes;
import org.aksw.jenax.model.shacl.util.PrefixSetUtils;
import org.aksw.jenax.model.shacl.util.ShSparqlTargets;
import org.aksw.jenax.sparql.relation.api.BinaryRelation;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.SortCondition;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.aggregate.AggMin;
import org.apache.jena.sparql.sse.SSE;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.Template;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.model.SHSPARQLTarget;
import org.topbraid.shacl.model.impl.SHSPARQLTargetImpl;
import org.topbraid.shacl.vocabulary.SH;




public class EntityClassifier {

    public static final Property classifier = ResourceFactory.createProperty("http://jsa.aksw.org/classifier");


    protected Map<Node, Relation> idToCondition = new LinkedHashMap<>();

    //protected int entityKeyLength;
    protected List<Var> entityKeyVars;



    public EntityClassifier(Var var) {
        this(Collections.singletonList(var));
    }

    public EntityClassifier(List<Var> entityKeyVars) {
        super();
        this.entityKeyVars = entityKeyVars;
    }



    public EntityClassifier addCondition(Node conditionId, Relation relation) {
        idToCondition.put(conditionId, relation);
        return this;
    }

    /**
     * Creates a SPARQL query that yields for each entry in a relation of candidates
     * the ones that satisfy a certain condition.
     * The resulting relation has the same variables in the same order as the provided candidate relation;
     * the classification is a single additional column with a fresh variable name that is guaranteed to
     * not clash with any of the other mentioned variables
     *
     * SELECT DISTINCT ?x1 ... ?xn ?idNode  {
     *   candidates(?x1 ... ?xn)
     *     {
     *       condition1(?x1 ... ?xn)
     *       BIND(?idOfCondition1)
     *     }
     *     UNION
     *     ...
     *     UNION
     *     {
     *       conditionM(?x1 ... ?xn)
     *       BIND(?idOfConditionM)
     *     }
     *   }
     * }
     *
     * @param conn
     * @param candidates
     * @param idToCondition
     */
    protected Relation createClassifyingRelation() {


        Set<Var> blacklist = new HashSet<>();
//        blacklist.addAll(candidate.getVarsMentioned());
        blacklist.addAll(entityKeyVars);
        for (Relation c : idToCondition.values()) {
            blacklist.addAll(c.getVarsMentioned());
        }

        Var conditionVar = VarGeneratorBlacklist.create("conditionId", blacklist).next();

        List<Element> unionMembers = new ArrayList<>(idToCondition.size());

        for (Entry<Node, ? extends Relation> e : idToCondition.entrySet()) {
            Node cId = e.getKey();
            Relation c = e.getValue();

            Relation part = c.rename(entityKeyVars); // e.getVars()
            List<Element> elts = part.getElements();
            elts.add(new ElementBind(conditionVar, NodeValue.makeNode(cId)));
            unionMembers.add(ElementUtils.groupIfNeeded(elts));
        }

        Element union = ElementUtils.unionIfNeeded(unionMembers);

//        Element finalEl = ElementUtils.groupIfNeeded(
//            candidate.getElement(),
//            union
//        );

        List<Var> finalVars = new ArrayList<>(entityKeyVars);
        finalVars.add(conditionVar);
        Relation result = new RelationImpl(union, finalVars);
        return result;
    }


    /**
     * Creates a graph fragment of the form
     *
     * _:X a Classification
     *   :of entityNode
     *   :keys (...)
     *   :classifier ?t
     *
     * @param candidate
     * @return
     */
    protected EntityGraphFragment createGraphFragmentGeneric(Relation candidate) {
        throw new RuntimeException("not implemented");
    }


    /**
     * Creates a graph fragment of the form
     *
     * { ?entity :classifier ?t }
     *
     * Cannot classify literals (because they may not appear in the subject position)
     *
     * @param candidate
     * @return
     */
    public EntityGraphFragment createGraphFragment() {
        BinaryRelation r = createClassifyingRelation().toBinaryRelation();

        Var entityVar = r.getSourceVar();
        Var classVar = r.getTargetVar();

        EntityTemplate et = new EntityTemplateImpl(Collections.singletonList(entityVar),
                new Template(BasicPattern.wrap(Arrays.asList(new Triple(entityVar, classifier.asNode(), classVar)))),
                new LinkedHashMap<>());

        EntityGraphFragment egf = new EntityGraphFragment(
                entityKeyVars,
                et,
                r.getElement());

        return egf;
    }


    public Map<Node, Relation> getIdToCondition() {
        return idToCondition;
    }


    public EntityQueryBasic toEntityQuery(Relation candidates) {
        return null;
        //new MapServiceSparqlQuery(qef, attrQuery, attrVar, isLeftJoin)
        ///createClassifyingRelation(candidates);
    }


//    public LookupService<Node, Node> createLookupService(SparqlQueryConnection conn, ResourceConditionMatcher matcher) {
//
//    	matcher.createClassifyingRelation(candidate)
//
//    	new ListService<C, T>() {
//		};
//
//    	return new LookupServiceSparqlQuery(sparqlService, query, var)
//
//    }
//

//    public static Table execSelectTable(SparqlQueryConnection conn, Relation relation) {
//        return execSelectTable(conn, relation.toQuery());
//    }
//
//    public static Table execSelectTable(Relation relation) {
//        return execSelectTable(relation.toQuery());
//    }
//
//
//    public static Table execSelectTable(Query query) {
//        Table result;
//        try (RDFConnection conn = RDFConnectionFactory.connect(DatasetFactory.create())) {
//            result = execSelectTable(conn, query);
//        }
//        return result;
//    }

    public static void main(String[] args) {

        EntityClassifier entityClassifier = new EntityClassifier(Arrays.asList(Vars.s));

        SHFactory.ensureInited();
        Model shaclModel = RDFDataMgr.loadModel("/home/raven/Projects/Eclipse/rmltk-parent/r2rml-resource-shacl/src/main/resources/r2rml.core.shacl.ttl");
        List<SHNodeShape> nodeShapes = shaclModel.listResourcesWithProperty(SH.property).mapWith(r -> r.as(SHNodeShape.class)).toList();

        // https://www.w3.org/TR/shacl/#targets
        // "The target of a shape is the union of all RDF terms produced by the individual targets that are declared by the shape in the shapes graph."
        for (SHNodeShape nodeShape : nodeShapes) {
            Set<Resource> extraTargets = new SetFromPropertyValues<>(nodeShape, SH.target, Resource.class);
            for (Resource extraTarget : extraTargets) {
//            	  sh:prefixes rs:rr ;
//            	  sh:select
                // SH.prefixes;
                // SH.se
                // SHACLUtil.isSPARQLProperty(classifier)
                Query query = ShSparqlTargets.tryParseSparqlQuery(extraTarget);
                if (query != null) {
                    System.out.println(query);

                    entityClassifier.addCondition(nodeShape.asNode(), RelationUtils.fromQuery(query));
                }

            }


            nodeShape.listProperties(SH.target);

            nodeShape.listProperties(SH.targetNode);
            nodeShape.listProperties(SH.targetClass);
            nodeShape.listProperties(SH.targetSubjectsOf);
            nodeShape.listProperties(SH.targetObjectsOf);
        }

        entityClassifier.addCondition(NodeFactory.createURI("urn:satisfies:hasLabel"),
                Concept.parse("?s { ?s rdfs:label ?l }"));

        entityClassifier.addCondition(NodeFactory.createURI("urn:satisfies:HasType"),
                Concept.parse("?x { ?x a ?t }"));

        EntityGraphFragment entityGraphFragment = entityClassifier.createGraphFragment();

        Graph graph = SSE.parseGraph("(graph (:e1 rdf:type :t) (:e2 rdfs:label :l) (:e3 rdfs:label :l) (:e3 rdf:type :t) (:e4 rdfs:comment :c) )");


        Model model = ModelFactory.createModelForGraph(graph);
        SparqlQueryConnection conn = RDFConnectionFactory.connect(DatasetFactory.wrap(model));

        Query concept = QueryFactory.create("SELECT DISTINCT ?s { ?s ?p ?o }");

        boolean materialize = false;
        if (materialize) {
            Query c = concept;
            Table table = QueryExecutionUtils.execSelectTable(() -> QueryExecutionFactory.create(c, model));

            Query tmp = QueryFactory.create("SELECT DISTINCT ?s {}");
            tmp.setQueryPattern(new ElementData(table.getVars(), Lists.newArrayList(table.rows())));
            concept = tmp;
        }


        // raw.setValuesDataBlock(table.getVars(), Lists.newArrayList(table.rows()));
        EntityBaseQuery ebq = new EntityBaseQuery(Collections.singletonList(Vars.s), new EntityTemplateImpl(), concept);

        Expr partitionSortExpr = new ExprAggregator(Var.alloc("dummy"),
                new AggMin(new E_Str(new ExprVar(Vars.o))));
        ebq.getPartitionOrderBy().add(new SortCondition(partitionSortExpr, Query.ORDER_ASCENDING));

//        ebq.getStandardQuery().setOffset(5);
//        ebq.getStandardQuery().setLimit(3);


        EntityQueryImpl eq = new EntityQueryImpl();
        eq.setBaseQuery(ebq);
        eq.getMandatoryJoins().add(new GraphPartitionJoin(entityGraphFragment));

        EntityQueryBasic basic = EntityQueryRx.assembleEntityAndAttributeParts(eq);

        EntityQueryRx.execConstructEntitiesNg(conn::query, basic).forEach(quad -> System.out.println(quad));


        //System.out.println(entityGraphFragment);
//        System.out.println(basic);
    }

}
