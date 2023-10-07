package org.aksw.jenax.facete.treequery2.api;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.aksw.facete.v4.impl.PropertyResolverImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jenax.facete.treequery2.impl.FacetConstraints;
import org.aksw.jenax.facete.treequery2.impl.Partition;
import org.aksw.jenax.facete.treequery2.impl.QueryContextImpl;
import org.aksw.jenax.facete.treequery2.impl.RelationQueryImpl;
import org.aksw.jenax.path.core.FacetPath;
import org.aksw.jenax.path.core.FacetStep;
import org.aksw.jenax.sparql.relation.api.Relation;
import org.aksw.jenax.treequery2.old.NodeQueryOld;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.syntax.ElementGroup;

public interface RelationQuery
    extends HasSlice
{
    /** A name to use for prefixing variables of this node's relation */
    String getScopeBaseName();

    /**
     * Returns the parent relation and a variable of it to which this relation connects.
     * Returns null if there is no parent
     */
    NodeQuery getParentNode();

    /** The step by which this relation was reached from the parent node (null if there is no parent) */
    FacetStep getReachingStep();

    /** Unlink this RelationQuery from its parent.
     *  Returns this.
     *  {@link #getParentNode()} will return null after this operation.
     */
    // RelationQuery detach();

    // RelationQuery clone();

    /** Get the parent relation of this instance */
    // RelationQuery parent();

    // FIXME resolve naming issue with roots()
    default RelationQuery root() {
        NodeQuery parentNode = getParentNode();
        RelationQuery r = parentNode == null ? this : parentNode.relationQuery().root();
        return r;
    }

    List<SortCondition> getSortConditions();

    /**
     * Returns the relation on which this RelationQuery is based.
     */
    Relation getRelation();

    /**
     * The context of this query which for example holds the PropertyResolver
     * for mapping properties to graph patterns.
     */
    QueryContext getContext();

    /**
     * Return this relation's partitioning, null if not partitioned.
     */
    Partition partition();

    /** Get the constraints that were placed on this relation */
    FacetConstraints<ConstraintNode<NodeQuery>> getFacetConstraints();

    /**
     * Declare this relation to be partitioned by the given paths.
     */
    void partitionBy(NodeQueryOld ... paths);


    /** Each row of a relation can be viewed as a node that has the column values as properties */
    NodeQueryOld asNode();

    /**
     * Return the facet path.
     * By convention, a facet step with the targetComponent set to null refers to a relation.
     * The 'targetComponent' field is null for all steps
     */
    FacetPath getPath();

    /**
     * Return each of the relation's columns as a QueryNode.
     */
    List<NodeQuery> roots();

    NodeQuery source();
    NodeQuery target();
    NodeQuery predicate();

    /** Obtain a {@link NodeQuery} for one of the variables of the relation returned by {@link #getRelation()}. */
    NodeQuery nodeFor(Var var);

    /** Create a new relation query with an empty graph pattern. Further elements connect to the given startVar. */
    public static RelationQuery of(Var startVar) {
        return of(() -> new Concept(new ElementGroup(), startVar));
    }

    public static RelationQuery of(Relation relation) {
        return of(() -> relation);
    }

    public static RelationQuery of(Supplier<Relation> relation) {
        return of(relation, new QueryContextImpl(new PropertyResolverImpl()));
    }

//    public static RelationQuery of(Relation relation, QueryContext queryContext) {
//        return new RelationQueryImpl(() -> relation, queryContext);
//    }

    public static RelationQuery of(Supplier<Relation> relation, QueryContext queryContext) {
        String scopeBaseName = queryContext.getScopeNameGenerator().next();
        return new RelationQueryImpl(scopeBaseName, null, relation, null, queryContext, new HashMap<>());
    }

    public static void doSort(RelationQuery relationQuery, Expr expr, int sortDirection) {
        SortCondition sc = new SortCondition(expr, sortDirection);
        List<SortCondition> sortConditions = relationQuery.getSortConditions();
        // Expr ev = new ExprVar(var);
        int idx = IntStream.range(0, sortConditions.size()).filter(i -> sortConditions.get(i).getExpression().equals(expr)).findFirst().orElse(-1);
        if (idx < 0) {
            if (sortDirection != Query.ORDER_UNKNOW)
            sortConditions.add(sc);
        } else {
            if (sortDirection == Query.ORDER_UNKNOW) {
                sortConditions.remove(idx);
            } else {
                sortConditions.set(idx, sc);
            }
        }
    }

    public static int getSortDirection(RelationQuery relationQuery, Expr expr) {
        // Expr ev = new ExprVar(var);
        int result = relationQuery.getSortConditions().stream()
                .filter(sc -> sc.getExpression().equals(expr))
                .map(SortCondition::getDirection).findFirst()
                .orElse(Query.ORDER_UNKNOW);
        return result;
    }
}
