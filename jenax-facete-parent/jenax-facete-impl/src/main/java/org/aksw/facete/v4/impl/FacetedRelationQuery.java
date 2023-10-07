package org.aksw.facete.v4.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.FacetConstraints;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.TreeData;
import org.aksw.facete.v3.api.TreeQuery;
import org.aksw.facete.v3.api.TreeQueryImpl;
import org.aksw.facete.v3.api.TreeQueryNode;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jenax.arq.util.binding.TableUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * A relation together with a tree structure much like a rooted construct query.
 */
class TreeRelation {
    protected Relation relation;
    protected TreeQuery tree;
    protected BiMap<TreeQueryNode, Var> pathToVar;
    // protected ListMultimap<TreeQueryNode, TreeQueryNode> foo;

    public TreeRelation(Relation relation, TreeQuery tree, BiMap<TreeQueryNode, Var> pathToVar) {
        super();
        this.relation = relation;
        this.tree = tree;
        this.pathToVar = pathToVar;
    }

    public static Node varToIri(Var var) {
        Node result = NodeFactory.createURI("urn:x-jenax:var:" + var.getName());
        return result;
    }

    /** Turn each variable of the relation into a property of the tree */
    public static TreeRelation of(Relation relation) {
        TreeQuery tree = new TreeQueryImpl();
        List<Var> vars = relation.getVars();
        BiMap<TreeQueryNode, Var> pathToVar = HashBiMap.create();
        for (Var v : vars) {
            Node property = varToIri(v);
            FacetPath facetPath = FacetPath.newRelativePath(FacetStep.fwd(property, null));
            TreeQueryNode varNode = tree.root().resolve(facetPath);
            pathToVar.put(varNode, v);
        }
        return new TreeRelation(relation, tree, pathToVar);
    }

    public static Table varsToTable(Collection<Var> vars) {
        List<Node> nodes = vars.stream().map(TreeRelation::varToIri).collect(Collectors.toList());
        Table result = TableUtils.createTable(Vars.p, nodes);
        return result;
    }
}

/**
 * A faceted relation query is backed by a relation.
 * Faceted queries can be started over each of its columns in order to constrain the matching rows.
 * A faceted query is an intensional description of a set of resources, whereas if left unconstrained then the set of
 * resources are those present in the column.
 *
 * Similar to SPARQL queries, which have CONSTRUCT and SELECT query forms, there are different ways
 * for how to project the underlying bindings.
 * COLUMNS is akin to select queries, where the projection is specified as a flat list of facet paths.
 * TREE corresponds to a hierarchical table. In this format columns can not be arbitrarily arranged: It is only possible to
 * reorder siblings.
 *
 */
@Deprecated // Use RelationQuery
public class FacetedRelationQuery {

    /**
     * Controls the result type for this query
     * and which attributes are in effect.
     *
     */
    public enum QueryType {
        COLUMNS,
        TREE
    }

    protected Supplier<Relation> baseRelation;

    // Property connecting the root node of the query tree to the nodes that represent the initial variables
    public static final Node INITIAL_VAR = NodeFactory.createURI("urn:var");

    /**
     * The root of the tree is a 'super root'.
     * It's children are those mapped to visible variables of the base relation.
     */
    // protected TreeQuery treeQuery;

    /** Each variable has its own tree query structure */
    protected BiMap<Var, FacetedQuery> varToRoot = HashBiMap.create();

    // TODO I don't really like how the constraints reference the treeQuery - maybe they should be passed as an argument
    protected FacetConstraints constraints = new FacetConstraints();


    // protected Map<TreeQueryNode, Boolean> projection = new HashMap<>();
    protected List<FacetNode> projection = new ArrayList<>();

    // An injective mapping of facet paths to sparql variables
    // TODO Paths need to eventually be resolved against variables - so probably we need to scope variables by their root.
    // Yet, we could use a common name for every path
    // So the first level of FacetNodes that refers to the root variables is different from the other FacetNodes. But how to capture that?!
    // protected FacetPathMapping pathMapping;


    protected QueryType queryType;
    protected TreeData<TreeQueryNode> treeProjection = new TreeData<>();
    protected List<TreeQueryNode> listProjection = new ArrayList<>();

    protected String scopeName;

    /** The set of facet paths which to project. Applies to both tree and list projections. */
    protected Set<TreeQueryNode> isVisible = new LinkedHashSet<>();

    public boolean isVisible(FacetPath path) {
        TreeQueryNode node = constraints.getTreeQuery().root().resolve(path);
        return isVisible.contains(node);
    }

    public FacetedRelationQuery(Supplier<Relation> baseRelation) {
        this(baseRelation, "");
    }

    public FacetedRelationQuery(Supplier<Relation> baseRelation, String scopeName) {
        super();
        this.baseRelation = baseRelation;
        this.scopeName = scopeName;

//        Generator<Var> varGen = GeneratorFromFunction.createInt().map(i -> Var.alloc("vv" + i));
//        DynamicInjectiveFunction<FacetPath, Var> ifn = DynamicInjectiveFunction.of(varGen);
//        FacetPathMapping fpm = ifn::apply;
//
//
//        // TODO Use a global path mapping by default
//        this.pathMapping = fpm;
    }

    public String getScopeBaseName() {
        return scopeName;
    }

    public List<FacetNode> getProjection() {
        return projection;
    }

    public static FacetedRelationQuery of(Relation relation) {
        return new FacetedRelationQuery(() -> relation);
    }

    /** Convenience function if there is only a single root variable */
    public FacetedQuery getFacetedQuery() {
        Relation relation = baseRelation.get();
        Var rootVar = null;
        if (relation.getVars().size() == 1) {
            rootVar = relation.getVars().iterator().next();
        }
        // Var rootVar = null;
        return getFacetedQuery(rootVar);
    }

    public FacetedQuery getFacetedQuery(Var var) {
        // TODO Check whether is part of the base relation
        // FIXME Do not traverse the given variable! Create a new scoped path that starts at that variable
        FacetedQuery result = varToRoot.computeIfAbsent(var, v -> {
            // FIXME We would have to allocate a fresh var
            // FacetStep step = FacetStep.fwd(INITIAL_VAR, null);
            // FacetStep step = FacetStep.fwd(v, null);
            TreeQueryNode node = constraints.getTreeQuery().root(); //.getOrCreateChild(step);
            return new FacetedQueryImpl(this, v, node);
        });
        return result;
    }


    /**
     * Create a new faceted relation query with all constraints applied.
     * The returned query's constraints are thus empty.
     * The contained graph pattern may be used for querying or for further faceted search.
     * This method is akin to FacetNode.availableValues()
     */
    public FacetedRelationQuery availableRows() {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        FacetedRelationQuery frq = FacetedRelationQuery.of(ConceptUtils.createSubjectConcept());
        FacetedQuery fq = frq.getFacetedQuery();

        fq.root()
            .fwd(RDF.type).viaAlias("a")
                .enterConstraints()
                    .exists().activate()
                    // .eq(OWL.Class).activate()
                .leaveConstraints()
            .parent()
            .fwd(RDF.type).viaAlias("b")
            .enterConstraints()
                .eq(OWL.Class).activate()
            .leaveConstraints()
            .parent()
            .fwd(FOAF.knows).one().fwd(RDF.type).one()
                .enterConstraints()
                    .eq(FOAF.Person).activate()
                    .eq(FOAF.Agent).activate()
                .leaveConstraints()
            .fwd(RDFS.label).one()
                .enterConstraints()
                    .absent().activate()
                .leaveConstraints()
            .parent()
            .fwd(FOAF.name).one()
                .enterConstraints()
                    .regex("hello").activate()
                    // .exists().activate()
                .leaveConstraints()
            .parent()
            .availableValues();
            // .fwd()
                //.facetCounts();


//    	FacetedQuery fq = new FacetedQueryImpl();
//        fq.root().fwd(RDF.type).one().bwd(RDFS.label).one().availableValues();

    }

}



