package org.aksw.jena_sparql_api.data_query.api;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.aksw.commons.util.range.CountInfo;
import org.aksw.facete.v3.api.FacetValue;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.aksw.jenax.arq.util.expr.ExprListUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RDFDataSources;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.FragmentImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_NotOneOf;
import org.apache.jena.sparql.expr.E_OneOf;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.ExprVars;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sparql.util.NodeUtils;

import com.google.common.collect.Iterables;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;


// This is similar to relationjoiner
//interface DataQueryJoiner<T extends RDFNode> {
//	DataQuery<T> with(Relation relation);
//}


// Actually, this is pretty much a resource

/**
 * This object represents a query builder for exposing a correlated join between
 * two graph-patterns based relations as partitions of rooted graph fragments.
 *
 * The first graph relation is referred to as the base relation. The other is the
 * attribute extension relation (AER). The AER is initially a unit table.
 *
 *
 * Query builder for retrieval batch retrieval of related *optional* information for each entity of an underlying
 * base relation.
 *
 * Hence, limit and offset apply to the base relation.
 *
 *
 *
 *
 *
 * @author raven
 *
 */
public interface DataQuery<T extends RDFNode> {
    // For every predicate, list how many root root resources there are having this predicate
    //getPredicatesAndRootCount();

    DataQuery<T> pseudoRandom(Random pseudoRandom);


    /**
     * Pass 'this' to a given consumer that may invoke methods on this object.
     * A typical use case is to print out the SPARQL query before execution
     * without breaking the chain of method calls:
     *
     * dataQuery
     *   .limit(10)
     *   .mutate(x -> System.out.println(x.toConstructQuery())
     *   .exec();
     *
     * @param consumer
     * @return
     */
    DataQuery<T> peek(Consumer<? super DataQuery<T>> consumer);

    /**
     * Access an attribute of the result class based on the
     * mapping between attributes and properties, such as using the @Iri annotation
     * Only works for appropriately annotated or mapped result classes
     * See {@link FacetValue} for an example.
     *
     * @param attrName
     * @return
     */
    NodePath get(String attrName);


    /**
     * Access an attribute of the result class based on the
     * mapping between attributes and properties, such as using the @Iri annotation
     * Only works for appropriately annotated or mapped result classes
     * See {@link FacetValue} for an example.
     *
     * @param attrName
     * @return
     */
    DataQueryVarView<T> getAttr(String attrName);


    Single<Model> execConstruct();

    Fragment1 fetchPredicates();

    DataNode getRoot();

    //DataMultiNode add(Property property);
    // Add an im
    DataQuery<T> add(Property property);
    DataQuery<T> addOptional(Property property);


    // this is similar to source.joinOn(attrNames).with(relation)
    DataQuery<T> filterUsing(Fragment relation, String ... attrNames);



    // Return the same data query with intersection on the given concept
    DataQuery<T> filter(Fragment1 concept);

    default DataQuery<T> filter(String exprStr) {
        Expr expr = ExprUtils.parse(exprStr);
        return filter(expr);
    }

    public static Fragment1 toUnaryFiler(Expr expr) {
        Set<Var> vars = ExprVars.getVarsMentioned(expr);
        if(vars.size() != 1) {
            throw new IllegalArgumentException("Provided expression must contain exactly 1 variable");
        }

        Var var = Iterables.getFirst(vars, null);
        Fragment1 result = new Concept(new ElementFilter(expr), var);
        return result;
    }

//	default DataQuery<T> filter(Expr expr) {
//		UnaryRelation ur = toUnaryFiler(expr);
//
//		return filter(ur);
//	}


    /**
     * Filtering by an unary expression (i.e. just 1 variable allowed)
     *
     * @param expr
     * @return
     */
    DataQuery<T> filter(Expr expr);

    // Filter injection without renaming variables
    DataQuery<T> filterDirect(Element element);

    DataQuery<T> dataSource(RDFDataSource dataSource);
    RDFDataSource dataSource();

    @Deprecated
    default SparqlQueryConnection connection() {
        RDFDataSource dataSource = dataSource();
        SparqlQueryConnection result = Optional.ofNullable(dataSource).map(RDFDataSource::getConnection).orElse(null);
        return result;
    }

    @Deprecated
    default DataQuery<T> connection(SparqlQueryConnection connection) {
        return dataSource(RDFDataSources.ofQueryConnection(connection));
    }

    default DataQuery<T> only(Iterable<Node> nodes) {
        Expr e = new E_OneOf(new ExprVar(Vars.s), ExprListUtils.nodesToExprs(nodes));
        return filter(new Concept(new ElementFilter(e), Vars.s));
    }

    default DataQuery<T> only(Node ... nodes) {
        return only(Arrays.asList(nodes));
    }

    default DataQuery<T> only(RDFNode ... rdfNodes) {
        return only(Arrays.asList(rdfNodes).stream().map(RDFNode::asNode).collect(Collectors.toList()));
    }

    default DataQuery<T> only(String ... iris) {
        return only(NodeUtils.convertToListNodes(Arrays.asList(iris)));
    }



    default DataQuery<T> exclude(Iterable<Node> nodes) {
        Expr e = new E_NotOneOf(new ExprVar(Vars.s), ExprListUtils.nodesToExprs(nodes));
        return filter(new Concept(new ElementFilter(e), Vars.s));
    }

    default DataQuery<T> exclude(Node ... nodes) {
        return exclude(Arrays.asList(nodes));
    }

    default DataQuery<T> exclude(RDFNode ... rdfNodes) {
        return exclude(Arrays.asList(rdfNodes).stream().map(RDFNode::asNode).collect(Collectors.toList()));
    }

    default DataQuery<T> exclude(String ... iris) {
        return exclude(NodeUtils.convertToListNodes(Arrays.asList(iris)));
    }

    DataQuery<T> limit(Long limit);

    default DataQuery<T> limit(Integer limit) {
        return limit(limit == null ? null : limit.longValue());
    }

    DataQuery<T> offset(Long offset);

    default DataQuery<T> offset(Integer offset) {
        return offset(offset == null ? null : offset.longValue());
    }

    /**
     * grouped mode (false): default semantic of construct queries
     * partition mode (true): each row is individually mapped to a resource, used for facet value counts
     *
     * Partition mode is there for legacy design choices and may be deprecated,
     * going with default semantics are strongly encouraged
     */
//    DataQuery<T> partitionMode(boolean onOrOff);
//    boolean isPartitionMode();


    DataQuery<T> sample(boolean onOrOff);

    default DataQuery<T> sample() {
        return sample(true);
    }

    boolean isSampled();


    DataQuery<T> ordered(boolean onOrOff);

    default DataQuery<T> ordered() {
        return ordered(true);
    }

    boolean isOrdered();


    DataQuery<T> randomOrder(boolean onOrOff);

    default DataQuery<T> randomOrder() {
        return randomOrder(true);
    }

    boolean isRandomOrder();


    /**
     * Return a SPARQL construct query together with the designated root variable
     *
     * TODO Do we need to revise the return value to allow multiple root variables? Maybe yield a Relation instance?
     * @return
     */
    // TODO Probably we should just return a plain sparql query
    // the other attributes can be obtained from this class (DataQuery)
    QuerySpec toConstructQueryNew();

    Var getDefaultVar();

    /**
     * A node of the template from which all primary key variables are reachable by
     * paths
     *
     * @return
     */
    Node getSuperRootNode();


    // Will be removed - but some code needs to cleaned up for this to work
    @Deprecated
    default Entry<Node, Query> toConstructQuery() {
        QuerySpec qs = toConstructQueryNew();
        return new SimpleEntry<>(getDefaultVar(), qs.getQuery());
    }


    default Fragment baseRelation() {
        return new FragmentImpl(baseElement(), primaryKeyVars());
    }

    List<Var> primaryKeyVars();
    Element baseElement();

    Flowable<T> exec();


    /**
     * Count the number of resources matching this data query's configuration
     * using default request parameters.
     * The result may yield a partial count (the count upon reaching a timeout)
     *
     *
     * @return
     */
    Single<CountInfo> count();
    Single<CountInfo> count(Long distinctItemCount, Long rowCount);


    ResolverNode resolver();


    DataQuery<T> addOrderBy(Node node, int direction);

    default DataQuery<T> addOrderBy(Path path, int direction) {
        Node node = nodeForPath(path);
        return addOrderBy(node, direction);
    }

    /**
     * Wrap a path object as a Node such that it can be used in conventional
     * SPARQL expressions passed to this data query instance such as for filtering and sorting
     *
     * The data query implementation will substitute all paths with appropriate
     * variables when assembling the effective sparql query in .toConstructQuery()
     *
     * @param path
     * @return
     */
    Node nodeForPath(Path path);


    <U extends RDFNode> DataQuery<U> as(Class<U> clazz);

//    default Node nodeForPath(Path path) {
//        return new NodePathletPath(path);
//    }
}
