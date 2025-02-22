package org.aksw.jena_sparql_api.data_query.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.aksw.commons.beans.model.EntityModel;
import org.aksw.commons.beans.model.EntityOps;
import org.aksw.commons.beans.model.PropertyOps;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.trees.TreeUtils;
import org.aksw.commons.util.range.CountInfo;
import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.facete.v3.api.path.StepImpl;
import org.aksw.facete.v3.experimental.ResolverNodeImpl;
import org.aksw.facete.v3.experimental.ResolverTemplate;
import org.aksw.facete.v3.experimental.Resolvers;
import org.aksw.jena_sparql_api.algebra.utils.AlgebraUtils;
import org.aksw.jena_sparql_api.data_query.api.DataNode;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.api.DataQueryVarView;
import org.aksw.jena_sparql_api.data_query.api.NodePath;
import org.aksw.jena_sparql_api.data_query.api.PathAccessor;
import org.aksw.jena_sparql_api.data_query.api.QuerySpec;
import org.aksw.jena_sparql_api.data_query.api.QuerySpecImpl;
import org.aksw.jena_sparql_api.data_query.api.ResolverNode;
import org.aksw.jena_sparql_api.data_query.api.SPath;
import org.aksw.jena_sparql_api.mapper.impl.type.RdfTypeFactoryImpl;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.aksw.jena_sparql_api.pathlet.PathletJoinerImpl;
import org.aksw.jena_sparql_api.pathlet.PathletSimple;
import org.aksw.jena_sparql_api.relationlet.RelationletElementImpl;
import org.aksw.jena_sparql_api.relationlet.RelationletSimple;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.triple.TripleUtils;
import org.aksw.jenax.arq.util.var.VarGeneratorBlacklist;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactories;
import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.FragmentImpl;
import org.aksw.jenax.sparql.query.rx.SparqlRx;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.algebra.optimize.Rewrite;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingComparator;
import org.apache.jena.sparql.expr.E_Random;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggSample;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.sparql.util.VarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/**
 * A more general view on path steps:
 * What bothers me with the current path approach is, that it does not allow one to navigate to or along
 * a predicate object of the *same* triple. Not that this is a commonly needed operation, but it
 * shows a shortcoming of the approach.
 *
 * "?s ?o WHERE ?s ?p ?o": How to navigate to ?p ?
 *
 *
 * Path steps are operations on a component-based binary relation
 * Component-based means, that we are referring to a specific component of a triple pattern in that relation
 * - in constrast to a specific variable within a BGP (which can occur in many components).
 *
 *
 * Given a binary relation ?s ?o WHERE { ?s ?p ?o },
 * we could have an operation that modifies the relation target
 * from e.g. the object of a triple pattern to the predicate component:
 *
 * toTriplePredicate(?s ?o WHERE { ?s ?p ?o }) -&gt; (?s ?p WHERE { ?s ?p ?o })
 *
 * Following a predicate could then be realized using a preconfigured operator created using a function:
 * stepFwd('foo:bar')(?s ?o WHERE { ?s ?p ?o }) -&gt; ?s ?z WHERE { ?s ?p ?o . ?o foo:bar ?z}
 *
 * In any case, the questions that arise are:
 * - When we allow traversals between ternary relations (of which triple patterns are a special case), then one component is always the source one,
 * where as two components remain
 *
 * Well, we don't need and want to modify the concept that a path intensionally connects two sets of RDF terms.
 * What is missing, is a triple-based traversal, where its possible to select the predicate or 'target' component
 * (and possibly add some constraint on these components
 *
 * But still, it leads to point, where a path segment corresponds to either
 * (a) the addition of a ternary relation or
 * (b) switching to a different component within this added relation
 * Then again, since we are using ternary relation objects, we can easily navigate to the subject, target, or source component
 *
 *
 */

/**
 * Optional paths
 *
 * Given the following graph patterns, are the some algebraic relations that hold between them?
 *
 * (a) { OPTIONAL { X } }
 * (b) { OPTIONAL { X OPTIONAL Y } }
 * (c) { OPTIONAL { X Y } }
 *
 * c: If for some binding of X there is no suitable binding of Y, the whole binding will be dropped
 *
 *
 * Maybe optional should be part of the traversal api?
 * fwd('foo').opt().fwd('bar')
 *
 *
 *
 */

public class DataQueryImpl<T extends RDFNode>
    implements DataQuery<T>
{
    // TODO Actually, there should be no logger here - instead there
    // should be some peekQuery(Consumer<Query>) if one wants to know the query
    private static final Logger logger = LoggerFactory.getLogger(DataQueryImpl.class);


    protected RDFDataSource dataSource;

    /**
     * grouped mode (false): default semantic of construct queries
     * partition mode (true): each row is individually mapped to a resource, used for facet value counts
     */
    // protected boolean isPartitionMode = false;

//	protected Node rootVar;
//	protected Element baseQueryPattern;


    // FIXME for generalization, probably this attribute has to be replaced by
    // a something similar to a list of roots; ege DataNode
    //protected Relation baseRelation;
    protected Element baseElement;
    protected List<Var> primaryKeyVars;

    // A node of the template that acts as the root.
    // Can be a variable but it may also be a blank node or constant.
    protected Node superRootNode;

    /**
     *
     * filter/only affect this variable by default
     * convenient if there is more than one primary key variable,
     *
     */
    protected Var defaultVar;


    protected Template template;

    protected List<DataNode> dataNodes;

//	protected Range<Long> range;

    protected Long limit;
    protected Long offset;

    protected Fragment1 filter;

    protected List<Element> directFilters = new ArrayList<>();

    protected boolean ordered;
    protected boolean randomOrder;
    protected boolean sample;


    protected Random pseudoRandom = null;


    protected Class<T> resultClass;

    protected List<SortCondition> sortConditions = new ArrayList<>();
    protected Set<Path> projectedPaths = new LinkedHashSet<>();

    public DataQueryImpl(
            RDFDataSource dataSource,
            Fragment1 baseRelation,
            Template template,
            Class<T> resultClass) {
        this(
                dataSource,
                baseRelation.getElement(),
                baseRelation.getVar(),
                template,
                resultClass);
    }

    public DataQueryImpl(
            RDFDataSource dataSource,
            Element baseQueryPattern,
            Var rootVar,
            Template template,
            Class<T> resultClass) {
        this(
                dataSource,
                baseQueryPattern,
                Arrays.asList(rootVar),
                rootVar,
                rootVar,
                template,
                resultClass);
    }

    public DataQueryImpl(
            RDFDataSource dataSource,
            Element baseElement,
            List<Var> primaryKeyVars,
            Node superRootNode,
            Var defaultVar,
            Template template,
            Class<T> resultClass) {
        super();
        this.dataSource = dataSource;
//		this.rootVar = rootNode;
//		this.baseQueryPattern = baseQueryPattern;
        //this.baseRelation = baseRelation;
        this.baseElement = baseElement;
        this.primaryKeyVars = primaryKeyVars;
        this.superRootNode = superRootNode;
        this.defaultVar = defaultVar;
        this.template = template;
        this.resultClass = resultClass;
    }

    @Deprecated
    public DataQueryImpl(
            SparqlQueryConnection conn,
            Fragment1 baseRelation,
            Template template,
            Class<T> resultClass) {
        this(
                conn,
                baseRelation.getElement(),
                baseRelation.getVar(),
                template,
                resultClass);
    }

    @Deprecated
    public DataQueryImpl(
            SparqlQueryConnection conn,
            Element baseQueryPattern,
            Var rootVar,
            Template template,
            Class<T> resultClass) {
        this(
                conn,
                baseQueryPattern,
                Arrays.asList(rootVar),
                rootVar,
                rootVar,
                template,
                resultClass);
    }

    @Deprecated
    public DataQueryImpl(
            SparqlQueryConnection conn,
            Element baseElement,
            List<Var> primaryKeyVars,
            Node superRootNode,
            Var defaultVar,
            Template template,
            Class<T> resultClass) {
        this(RdfDataSources.ofQueryConnection(conn), baseElement, primaryKeyVars, superRootNode, defaultVar, template, resultClass);
    }



    // FIXME Add more structure to the attributes
//    public DataQueryImpl(SparqlQueryConnection conn, Element baseElement, List<Var> primaryKeyVars, Node superRootNode,
//			Var defaultVar, Template template, List<DataNode> dataNodes, Long limit, Long offset, UnaryRelation filter,
//			List<Element> directFilters, boolean ordered, boolean randomOrder, boolean sample, Random pseudoRandom,
//			Class<T> resultClass, List<SortCondition> sortConditions, Set<Path> projectedPaths) {
//		super();
//		this.conn = conn;
//		this.baseElement = baseElement;
//		this.primaryKeyVars = primaryKeyVars;
//		this.superRootNode = superRootNode;
//		this.defaultVar = defaultVar;
//		this.template = template;
//		this.dataNodes = dataNodes;
//		this.limit = limit;
//		this.offset = offset;
//		this.filter = filter;
//		this.directFilters = directFilters;
//		this.ordered = ordered;
//		this.randomOrder = randomOrder;
//		this.sample = sample;
//		this.pseudoRandom = pseudoRandom;
//		this.resultClass = resultClass;
//		this.sortConditions = sortConditions;
//		this.projectedPaths = projectedPaths;
//	}

    public <U extends RDFNode> DataQuery<U> as(Class<U> clazz) {
        return new DataQueryImpl<U>(
                dataSource,
                baseElement,
                primaryKeyVars,
                superRootNode,
                defaultVar,
                template,
                clazz);
    }

    @Override
    public RDFDataSource dataSource() {
        return dataSource;
    }

    @Override
    public DataQuery<T> dataSource(RDFDataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    @Override
    public Var getDefaultVar() {
        return defaultVar;
    }

    @Override
    public DataQuery<T> limit(Long limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public DataQuery<T> offset(Long offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public DataQuery<T> sample(boolean onOrOff) {
        this.sample = onOrOff;
        return this;
    }

    @Override
    public boolean isSampled() {
        return sample;
    }


    @Override
    public DataQuery<T> ordered(boolean onOrOff) {
        this.ordered = onOrOff;
        return this;
    }

    @Override
    public boolean isOrdered() {
        return ordered;
    }

    @Override
    public boolean isRandomOrder() {
        return randomOrder;
    }

    @Override
    public DataQuery<T> randomOrder(boolean onOrOff) {
        this.randomOrder = onOrOff;
        return this;
//		return this;
    }

    //protected void setOffset(10);


    /**
     * Setting a random number generator (rng) makes query execution deterministic:
     * Random effects on result sets will be processed in the client:
     * Randomly ordered result sets will be fully loaded into the client and shuffeled in respect
     * to the given rng.
     *
     * TODO We may need extra an extra 'deterministic' attribute to indicate
     * whether to sort result sets - the problem is, that the same query on data loaded at different
     * times may yield different results. For practical purposes it rarely happens that the same query
     * yields different results if there were no changes in the data. But maybe it could happen
     * if a DB did something similar to postgres' vacuum process?
     *
     * @param pseudoRandom
     * @return
     */
    @Override
    public DataQuery<T> pseudoRandom(Random pseudoRandom) {
        this.pseudoRandom = pseudoRandom;
        return this;
    }


    @Override
    public Concept fetchPredicates() {
        // TODO Auto-generated method stub
        throw new RuntimeException("not implemented yet");
    }


    @Override
    public DataNode getRoot() {
        // TODO Auto-generated method stub
        throw new RuntimeException("not implemented yet");
    }

    @Override
    public DataQuery<T> addOptional(Property property) {
        Path path = Path.newPath().optional(property).fwd(property);
        projectedPaths.add(path);

        return this;
    }

    @Override
    public DataQuery<T> add(Property property) {
        Path path = Path.newPath().fwd(property);
        projectedPaths.add(path);

        return this;
//		Var rootVar = getRootVar();
//
//		resolver().fwd().
//
//		//Triple t = new Triple(rootVar, property.asNode(), NodePathletPath.create());
//
//		template.getBGP().add(t);
//
//
//		//resolver().fwd(property).one().
//
//		// TODO Auto-generated method stub
//		return null;
    }


    @Override
    public DataQuery<T> filter(Fragment1 concept) {
        if(concept != null) {
            if(filter == null) {
                filter = concept;
            } else {
                filter = filter.joinOn(filter.getVar()).with(concept).toFragment1();
            }
        }

        return this;
    }

    @Override
    public DataQuery<T> filterDirect(Element element) {
        directFilters.add(element);

        return this;
    }



    @Override
    public DataQuery<T> peek(Consumer<? super DataQuery<T>> consumer) {
        consumer.accept(this);
        return this;
    }

    /**
     * Join the base element on vars corresponding to attrName with another relation
     *
     * Method is subject to be replaced with a more general mechanism based on Relationlets
     */
    @Override
    public DataQuery<T> filterUsing(Fragment relation, String... attrNames) {

        if(relation != null) {
            List<Var> vars = Arrays.asList(attrNames).stream()
                    .map(this::resolveAttrToVar)
                    .collect(Collectors.toList());

            List<Var> baseVars = new ArrayList<>(PatternVars.vars(baseElement));
            baseElement = new FragmentImpl(baseElement, baseVars).joinOn(vars).with(relation).getElement();
        }
        return this;
    }

    public Var resolveAttrToVar(String attr) {
        EntityOps entityOps = EntityModel.createDefaultModel(resultClass, null);

        PropertyOps pops = entityOps.getProperty(attr);
        String iri = RdfTypeFactoryImpl.createDefault().getIri(entityOps, pops);

        // FIXME HACK We need to start from the root node instead of iterating the flat list of triple patterns
        Triple match = template.getBGP().getList().stream()
            .filter(t -> t.getPredicate().getURI().equals(iri))
            .findFirst()
            .orElse(null);

        Node node = Optional.ofNullable(match.getObject())
                .orElseThrow(() -> new RuntimeException("No member with name " + attr + " in " + resultClass));

        Var result = (Var)node;
        return result;
    }


    /**
     * Hibernate-like get method which resolves an attribute of the resultClass
     * to a SPARQL variable of the underlying partitioned query's template
     *
     * The result is a Node that can be used in Jena SPARQL {@link Expr} expressions,
     * which can be subsequently passed to e.g. filter(expr) of this class for filtering.
     *
     */
    @Override
    public NodePath get(String attr) {

        Var var = resolveAttrToVar(attr);
//		Node node = Optional.ofNullable(iri)
//				.map(NodeFactory::createURI)
//				.orElseThrow(() -> new MemberNotFoundException("No member with name " + attr + " in " + resultClass));

//
//
//		Node node = Optional.ofNullable(pops)
//			.map(p -> p.findAnnotation(Iri.class))
//			.map(Iri::value)
//			.map(NodeFactory::createURI)
//			.orElseThrow(() -> new MemberNotFoundException("No member with name " + attr + " in " + resultClass));

//		System.out.println("Found: " + node);

        Model m = ModelFactory.createDefaultModel();
        SPath tmp = new SPathImpl(m.createResource().asNode(), (EnhGraph)m);
        tmp.setAlias(var.getName());

//		tmp = tmp.get(node.getURI(), false);


        //tmp.setAlias(alias);

        NodePath result = new NodePath(tmp);

        return result;
    }



    @Override
    public DataQuery<T> filter(Expr expr) {
        PathAccessor<SPath> pathAccessor = new PathAccessorSPath();
        PathToRelationMapper<SPath> mapper = new PathToRelationMapper<>(pathAccessor, "w");

        Collection<Element> elts = FacetedQueryGenerator.createElementsForExprs(mapper, pathAccessor, Collections.singleton(expr), false);

        // FIXME Hack to obtain a zero-length path; equals on SPath is broken
        SPath root = PathAccessorUtils.getPathsMentioned(expr, pathAccessor::tryMapToPath).values().stream()
            .map(p -> TreeUtils.findRoot(p, pathAccessor::getParent))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Should not happen: Expr without path - " + expr));


        //SPath root = ModelFactory.createDefaultModel().createResource().as(SPath.class);
        //Var rootVar = (Var)pathAccessor.(root);

        if(false) {
        Var rootVar = (Var)mapper.getNode(root);

        Fragment1 tmp = new Concept(ElementUtils.groupIfNeeded(elts), rootVar);
        filter(tmp);
        } else {
            filterDirect(ElementUtils.groupIfNeeded(elts));
        }
        //TreeUtils.getRoot(item, predecessor)

        //NodeTransform xform = PathToRelationMapper.createNodeTransformSubstitutePathReferences(new PathAccessorSPath());

        //Expr e = expr.applyNodeTransform(xform);
        // Transformation has to be done using NodeTransformExpr as
        // it correctly substitutes NodeValues with ExprVars
//		Expr e = ExprTransformer.transform(new NodeTransformExpr(xform), expr);
//
//		UnaryRelation f = DataQuery.toUnaryFiler(e);
//		filter(f);

        return this;
    }



    // Note: The construct template may be empty - use in conjunction with ReactiveSparqlUtils.execPartitioned()
    // TODO Rename to getEffectiveQuery
//	@Override
//	public Entry<Node, Query> toConstructQuery() {
//		// This method perform in-place transform on the query object
//		Entry<Node, Query> tmp = toBaseConstructQuery();
//
//		Var rootVar = (Var)tmp.getKey();
//		Query baseQuery = tmp.getValue();
//		Resolver resolver = Resolvers.from(rootVar, baseQuery);
//
//		PathletContainerImpl pathlet = new PathletContainerImpl(resolver);
//		// Add the base query to the pathlet, with variable ?s joining with the pathlet's root
//		// and ?s also being the connector for subsequent joins
//		pathlet.add(new PathletSimple(rootVar, rootVar, new RelationletElementImpl(baseQuery.getQueryPattern()).fixAll()));
//
//		// Substitute all NodePathletPath objects with NodePathletVarRef objects
//		NodeTransform xform1 = new NodeTransformPathletPathResolver(pathlet);
//		QueryUtils.applyNodeTransform(baseQuery, xform1);
//
//		// Now that all paths have been collected and added to the pathlet
//		// materalize it
//		RelationletNested rn = pathlet.materialize();
//
//		// Resolve all var refs against the materialized relationlet
//		NodeTransform xform2 = new NodeTransformPathletVarRefResolver(rn);
//		QueryUtils.applyNodeTransform(baseQuery, xform2);
//
//		Element e = rn.getElement();
//
//		baseQuery.setQueryPattern(e);
//
//		return tmp;
//	}


    @Override
    public Node getSuperRootNode() {
        return superRootNode;
    }

    public List<Var> getPrimaryKeyVars() {
        return primaryKeyVars;
//        Node rootNode = baseRelation.getVars().get(0);
//
//        return (Var)rootNode;
    }

    @Override
    public QuerySpec toConstructQueryNew() {

        Set<Var> vars = new LinkedHashSet<>();

//		System.out.println("Root vars: " + baseRelation.getVars());

//        Node rootVar = baseRelation.getVars().get(0);
//        if(rootVar.isVariable()) {
//            vars.add((Var)rootVar);
//        }

        vars.addAll(primaryKeyVars);

        //boolean canAsConstruct = template != null && !template.getBGP().isEmpty();

        // The query projection is set up once the effective template is created

        Query query = new Query();
        query.setQuerySelectType();

//		query.setQueryConstructType();
//		query.setConstructTemplate(template);


        //query.setQueryResultStar(true);
        //query.setQuerySelectType();

        // Start with a select query, optimize it, and at the end turn it into construct
//		query.setQuerySelectType();


        Element baseQueryPattern = baseElement();

        Element effectivePattern = filter == null
                ? baseQueryPattern
                : new FragmentImpl(baseQueryPattern, new ArrayList<>(PatternVars.vars(baseQueryPattern))).joinOn(defaultVar).with(filter).getElement()
                ;

        if(!directFilters.isEmpty()) {
            effectivePattern = ElementUtils.groupIfNeeded(Iterables.concat(Collections.singleton(effectivePattern), directFilters));
        }


        boolean deterministic = pseudoRandom != null;

        Set<Var> allVars = new LinkedHashSet<>();
        allVars.addAll(vars);
        // TODO We only need the pattern vars (or more specifically only the visible vars) if
        // sample is true or there are projected paths
        allVars.addAll(PatternVars.vars(baseQueryPattern));

        Generator<Var> varGen = VarGeneratorBlacklist.create(allVars);
        if(sample) {
            Var innerRootVar = varGen.next();

//			if(baseQueryPattern instanceof ElementSubQuery) {
//				QueryGroupExecutor.createQueryGroup()
//
//			}

            // Sampling only works on the superRootNode
            if(primaryKeyVars.size() != 1 || !primaryKeyVars.get(0).equals(superRootNode)) {
                throw new RuntimeException("Sampling can only be done if superRootNode is equal to the only primaryKeyVar " + superRootNode + " != " + primaryKeyVars);
            }

            Node rootVar = (Var)superRootNode;
            Element innerE = ElementUtils.createRenamedElement(effectivePattern, Collections.singletonMap(rootVar, innerRootVar));


            Query inner = new Query();
            inner.setQuerySelectType();
            inner.setQueryPattern(innerE);
            Expr agg = inner.allocAggregate(new AggSample(new ExprVar(innerRootVar)));
            for(Var primaryKeyVar : primaryKeyVars) {
                inner.getProject().add(primaryKeyVar, agg);
            }

            if(!(randomOrder && deterministic)) {
                QueryUtils.applySlice(inner, offset, limit, false);
            }

            Element e = ElementUtils.groupIfNeeded(new ElementSubQuery(inner), effectivePattern);

            query.setQueryPattern(e);
        } else {

            // TODO Controlling distinct should be possible on this class
            query.setDistinct(true);

            query.setQueryPattern(effectivePattern);

            if(!(randomOrder && deterministic)) {
                QueryUtils.applySlice(query, offset, limit, false);
            }
        }


        if(ordered) {
            for(Var primaryKeyVar : primaryKeyVars) {
                query.addOrderBy(primaryKeyVar, Query.ORDER_ASCENDING);
            }
        }

        if(randomOrder && !deterministic) {
            query.addOrderBy(new E_Random(), Query.ORDER_ASCENDING);
//			query.addOrderBy(new E_RandomPseudo(), Query.ORDER_ASCENDING);
        }


        for(SortCondition sc : sortConditions) {
            query.addOrderBy(sc);
        }


        // Create a copy of the template as the basis for the construction of the effective template
        Template effectiveTemplate = template != null
                ? new Template(new BasicPattern(template.getBGP()))
                : new Template(new BasicPattern());

        // Resolve paths
        {

            Query resolverConstruct = new Query();
            resolverConstruct.setQueryConstructType();
            resolverConstruct.setConstructTemplate(effectiveTemplate);
            resolverConstruct.setQueryPattern(effectivePattern);

            Resolver resolver = Resolvers.from(resolverConstruct, superRootNode);

            PathletJoinerImpl pathlet = new PathletJoinerImpl(resolver);
            // Add the base query to the pathlet, with variable ?s joining with the pathlet's root
            // and ?s also being the connector for subsequent joins

            // TODO Each path should be relative to another var or a path
            // and eventually the superRootNode
            Var pathRoot = defaultVar; // superRootNode instanceof Var ? (Var)superRootNode : defaultVar;
            pathlet.add(new PathletSimple(pathRoot, pathRoot, new RelationletElementImpl(query.getQueryPattern()).pinAllVars()));


            // Add all projected paths to the pathlet
            Set<Triple> newTemplateTriples = new LinkedHashSet<Triple>();
            //List<VarRefStatic> projectedVarRefs = new ArrayList<>();
            for(Path path : projectedPaths) {
                ResolverTemplate resolverTemplate = Resolvers.from(resolverConstruct, superRootNode);

                // Register the path with the pathlet
                pathlet.resolvePath(path);

                // Add any triples to the construct template if necessary
                List<StepImpl> steps = Path.getSteps(path);

                Node startNode = resolverTemplate.getStartNode();
                for(int i = 0; i < steps.size(); ++i) {
                    StepImpl step = steps.get(i);

                    if(step.getKey() instanceof P_Path0) {
                        // Here is the case for basic steps

                        P_Path0 stepPath = (P_Path0)step.getKey();
                        Node p = stepPath.getNode();
                        boolean isFwd = stepPath.isForward();

                        //BinaryRelation br = (BinaryRelation)step.getKey();
                        //RelationUtils.extractTriple(br);
                        String alias = step.getAlias();
                        ResolverTemplate next = resolverTemplate != null
                                ? resolverTemplate.resolveTemplateSimple(stepPath, alias)
                                : null;

                        if(next != null) {
                            startNode = next.getStartNode();
                            resolverTemplate = next;
                        } else {
                            Path endPath = Path.newPath();
                            for(StepImpl tmp : steps.subList(0, i)) {
                                endPath = endPath.appendStep(tmp);
                            }
                            endPath = endPath.appendStep(step);
                            Node endNode = NodePathletPath.create(endPath);

                            Triple t = TripleUtils.create(
                                    startNode,
                                    p,
                                    endNode,
                                    isFwd);
                            newTemplateTriples.add(t);
                        }
                    }
                }


//				VarRefStatic varRef = pathlet.resolvePath(path).get();
//				projectedVarRefs.add(varRef);
            }

            BasicPattern bgp = effectiveTemplate.getBGP();
            for(Triple t : newTemplateTriples) {
                bgp.add(t);
            }


            // Substitute all NodePathletPath objects with NodePathletVarRef objects
            NodeTransform xform1 = new NodeTransformPathletPathResolver(pathlet);
            query = QueryUtils.applyNodeTransform(query, xform1);
            effectiveTemplate = applyNodeTransform(effectiveTemplate, xform1);


            // Now that all paths have been collected and added to the pathlet
            // materialize it
            RelationletSimple rn = pathlet.materialize();

            // Resolve all var refs against the materialized relationlet
            NodeTransform xform2 = new NodeTransformPathletVarRefResolver(rn);
            query = QueryUtils.applyNodeTransform(query, xform2);
            effectiveTemplate = applyNodeTransform(effectiveTemplate, xform2);

            Element e = rn.getElement();


            VarUtils.addVars(vars, effectiveTemplate.getBGP());

            VarExprList vel = query.getProject();
            for(Var v : vars) {
                vel.add(v);
            }

            query.setQueryPattern(e);
        }




        //logger.info("Generated query: " + query);

        Rewrite rewrite = AlgebraUtils.createDefaultRewriter();
        query = QueryUtils.rewrite(query, rewrite::rewrite);

        // NOTE: The CONSTRUCT part gets lost in the rewriting, restore it

        //if(canAsConstruct) {
        //}


        Query c = QueryUtils.selectToConstruct(query, effectiveTemplate);

//		logger.debug("After rewrite: " + query);


        // Pattern p = Pattern.compile("^.*v_2\\s*<[^>]*>\\s*v_2.*$", Pattern.MULTILINE);
        // if(p.matcher("" + query).find()) {
        // 	System.out.println("DEBUG POINT reached");
        // }

        //return Maps.immutableEntry((Var)rootVar, c);
        return new QuerySpecImpl(c, superRootNode, primaryKeyVars);
    }

    public static Template applyNodeTransform(Template template, NodeTransform xform) {
        BasicPattern before = template.getBGP();
        BasicPattern after = NodeTransformLib.transform(xform, before);
        Template result = new Template(after);
        return result;
    }

    /**
     * Strip a path from steps of non-basic steps, such as optional ones.
     *
     */
    public static List<Entry<P_Path0, String>> toSimpleSteps(Path path) {
        List<Entry<P_Path0, String>> result = new ArrayList<>();
        for(StepImpl step : Path.getSteps(path)) {
            Object key = step.getKey();
            if(key instanceof P_Path0) {
                String alias = step.getAlias();
                P_Path0 p = (P_Path0)key;

                result.add(Maps.immutableEntry(p, alias));
            }
        }

        return result;
    }

    @Override
    public Flowable<T> exec() {
        Objects.requireNonNull(dataSource);

        QuerySpec e = toConstructQueryNew();
//		Node rootVar = e.getKey();
//		Query query = e.getValue();

        // PseudoRandomness affects:
        // limit, offset, orderByRandom, ... what else?


        logger.debug("Executing query:\n" + e);
//
//		Flowable<T> result = ReactiveSparqlUtils
//			// For future reference: If we get an empty results by using the query object, we probably have wrapped a variable with NodeValue.makeNode.
//			.execSelect(() -> conn.query(query))
//			.map(b -> {
//				Graph graph = GraphFactory.createDefaultGraph();
//
//				// TODO Re-allocate blank nodes
//				if(template != null) {
//					Iterator<Triple> it = TemplateLib.calcTriples(template.getTriples(), Iterators.singletonIterator(b));
//					while(it.hasNext()) {
//						Triple t = it.next();
//						graph.add(t);
//					}
//				}
//
//				Node rootNode = rootVar.isVariable() ? b.get((Var)rootVar) : rootVar;
//
//				Model m = ModelFactory.createModelForGraph(graph);
//				RDFNode r = m.asRDFNode(rootNode);
//
////				Resource r = m.createResource()
////				.addProperty(RDF.predicate, m.asRDFNode(valueNode))
////				.addProperty(Vocab.facetValueCount, );
////			//m.wrapAsResource(valueNode);
////			return r;
//
//				return r;
//			})

        // TODO Add the toggle to SparqlRx
        Flowable<Entry<Binding, RDFNode>> rawFlow =
                SparqlRx.execConstructGrouped(dataSource, e.getQuery(), e.getPrimaryKeyVars(), e.getRootNode(), true);


        boolean deterministic = pseudoRandom != null;

        if(deterministic && randomOrder) {
            List<SortCondition> scs = e.getPrimaryKeyVars().stream()
                    .map(v -> new SortCondition(v, Query.ORDER_ASCENDING))
                    .collect(Collectors.toList());

            Comparator<Binding> cmp = new BindingComparator(scs);

            rawFlow = rawFlow.toList().map(l -> {
                // Always sort the collection, so that subsequent shuffle will give the same result
                // regardless of initial order

                Collections.sort(l, (a, b) -> cmp.compare(a.getKey(), b.getKey()));
                Collections.shuffle(l, pseudoRandom);

                Range<Long> available = Range.closed(0l, (long)l.size());
                Range<Long> requested = QueryUtils.toRange(offset, limit);
                Range<Long> effective = available.intersection(requested);
                long o = effective.lowerEndpoint();
                long size = effective.upperEndpoint() - o;

                return l.subList((int)o, (int)size);

                //return subList;
            }).toFlowable().flatMap(Flowable::fromIterable);
        }

        Flowable<T> result = rawFlow
                .map(Entry::getValue)
                .map(r -> r.as(resultClass));


        return result;
    }


    /**
     * A template resolver enables traversal of the template and injection of
     * further triple patterns.
     *
     * So essentially it is the Resource API with the addition of allowing DataResolution
     * references.
     *
     *
     */
//	public TemplateResolver templateResolver() {
//
//	}


    public DataQuery<T> addOrderBy(Node node, int direction) {
        sortConditions.add(new SortCondition(node, direction));
        return this;
    }

    public DataQuery<T> addOrderBy(Path path, int direction) {
        return addOrderBy(new NodePathletPath(path), direction);
    }


//	public void addSortCondition() {
//		Query x;
//		x.orde
//		sortConditions.add(e)
//	}

    @Override
    public Element baseElement() {
        return baseElement;
    }

    @Override
    public List<Var> primaryKeyVars() {
        return primaryKeyVars;
    }


//    public Relation baseRelation() {
////		Element effectivePattern = filter == null
////				? baseQueryPattern
////				: new RelationImpl(baseQueryPattern, new ArrayList<>(PatternVars.vars(baseQueryPattern))).joinOn((Var)rootVar).with(filter).getElement()
////				;
//
//        //UnaryRelation result = new Concept(baseQueryPattern, (Var)rootVar);
//        return baseRelation;
//    }

    @Override
    public Single<Model> execConstruct() {
        return exec().toList().map(l -> {
            Model r = ModelFactory.createDefaultModel();
            for(RDFNode item : l) {
                Model tmp = item.getModel();
                if(tmp != null) {
                    r.add(tmp);
                }
            }
            return r;
        });
    }


    // TODO Move to Query Utils
//	public static Query rewrite(Query query, Rewrite rewrite) {
//		Query result = rewrite(query, (Function<? super Op, ? extends Op>)rewrite::rewrite);
//		result.getPrefixMapping().setNsPrefixes(query.getPrefixMapping());
//		return result;
//	}


    @Override
    public Single<CountInfo> count() {
        return count(null, null);
    }

    @Override
    public Single<CountInfo> count(Long distinctItemCount, Long rowCount) {
        QuerySpec e = toConstructQueryNew();

//        Set<Var> partitionVars = new LinkedHashSet<>();

        Query query = e.getQuery();
        query.setQuerySelectType();
        query.setDistinct(true);
        query.setQueryResultStar(false);
        query.getProject().clear();
        e.getPrimaryKeyVars().forEach(query.getProject()::add);

//        partitionVars.add((Var)e.getKey());
//        if(isPartitionMode) {
//            Set<Var> templateVars = QuadPatternUtils.getVarsMentioned(template.getQuads());
//            partitionVars.addAll(templateVars);
//        }
//        Query query = e.getValue();
        //		QueryExecutionUtils.countQuery(query, new QueryExecutionFactorySparqlQueryConnection(conn));
        Single<CountInfo> result = SparqlRx.fetchCountQueryPartition(QueryExecutionFactories.of(dataSource), query, e.getPrimaryKeyVars(), distinctItemCount, rowCount)
                .map(range -> CountUtils.toCountInfo(range));

        return result;
    }

    @Override
    public ResolverNode resolver() {
        QuerySpec e = toConstructQueryNew();
        // PartitionedQuery1 pq = PartitionedQuery1.from(e.getQuery(), e.getRootNode());

        ResolverNode result = ResolverNodeImpl.from(e.getQuery(), superRootNode, this);
        return result;
    }

//    @Override
//    public DataQuery<T> partitionMode(boolean onOrOff) {
//        this.isPartitionMode = onOrOff;
//        return this;
//    }

//    @Override
//    public boolean isPartitionMode() {
//        return isPartitionMode;
//    }

    @Override
    public Node nodeForPath(Path path) {
        return new NodePathletPath(path);
    }

    @Override
    public DataQueryVarView<T> getAttr(String attrName) {
        throw new RuntimeException("not implemented yet");
    }

}

