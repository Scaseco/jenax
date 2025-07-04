package org.aksw.jenax.sparql.query.rx;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.aksw.commons.collections.SetUtils;
import org.aksw.commons.rx.op.FlowableOperatorCollapseRuns;
import org.aksw.commons.rx.util.FlowableUtils;
import org.aksw.commons.util.stream.CollapseRunsSpec;
import org.aksw.jenax.arq.aggregation.AccGraph2;
import org.aksw.jenax.arq.util.binding.BindingUtils;
import org.aksw.jenax.arq.util.exception.HttpExceptionUtils;
import org.aksw.jenax.arq.util.quad.QuadPatternUtils;
import org.aksw.jenax.arq.util.syntax.QueryGenerationUtils;
import org.aksw.jenax.arq.util.var.VarUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactories;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecFactoryQuery;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryQuery;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.SortCondition;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.table.TableData;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIteratorResultSet;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.http.QueryExecHTTP;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.TemplateLib;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.processors.PublishProcessor;

/**
 * Utilities for wrapping SPARQL query execution with flows.
 *
 * @author raven
 *
 */
public class SparqlRx {

    private static final Logger logger = LoggerFactory.getLogger(SparqlRx.class);

    public static Single<Boolean> execAsk(Function<Query, QueryExecution> qeSupp, Query query) {
        return Flowable.<Boolean, QueryExecution>generate(
            () -> qeSupp.apply(query),
            (qe, e) -> {
                try {
                    boolean r = qe.execAsk();
                    e.onNext(r);
                    e.onComplete();
                } catch (Exception ex) {
                    e.onError(ex);
                }
            },
            QueryExecution::close)
                .singleOrError()
                .onErrorResumeNext(Single::error);
    }

    /**
     * Create a Flowable from a supplier of connections and a query.
     * Each subscription obtains a fresh connection
     *
     * FIXME Connection is not yet closed when the flowable completes!
     *
     * @param query
     * @param queryConnSupp
     * @return
     */
    public static Flowable<Binding> execSelectRaw(Callable<? extends SparqlQueryConnection> queryConnSupp, Query query) {
        // FIXME Close the connection; tie it to the query execution
        // Queryexecution qe = new QueryExecution();
        return SparqlRx.execSelectRaw(() -> queryConnSupp.call().query(query));
    }

    public static Flowable<Binding> execSelectRaw(QueryExecutionFactoryQuery qeSupp, Query query) {
        return execSelectRaw(() -> qeSupp.createQueryExecution(query));
    }

    public static Flowable<Binding> execSelectRaw(SparqlQueryConnection queryConn, Query query) {
        return SparqlRx.execSelectRaw(() -> queryConn.query(query));
    }

    public static <K, X> Flowable<Entry<K, List<X>>> groupByOrdered(
            Flowable<X> in, Function<X, K> getGroupKey) {

          Object[] current = {null};
          Object[] prior = {null};
          PublishProcessor<K> boundaryIndicator = PublishProcessor.create();

          return in
             .doOnComplete(boundaryIndicator::onComplete)
             .doOnNext(item -> {
                    K groupKey = getGroupKey.apply(item);
                    boolean isEqual = Objects.equals(current, groupKey);

                    prior[0] = current[0];
                    if(prior[0] == null) {
                        prior[0] = groupKey;
                    }

                    current[0] = groupKey;


                    if(!isEqual) {
                        boundaryIndicator.onNext(groupKey);
                    }
              })
              .buffer(boundaryIndicator)
              .map(buffer -> {
                  K tmp = (K)prior[0];
                  K groupKey = tmp;

                  return Maps.immutableEntry(groupKey, buffer);
              });

    }

    /**
     *
     *
     * @param vars
     * @return
     */
    public static Function<List<Binding>, Table> createTableBuffer(List<Var> vars) {
        Function<List<Binding>, Table> result = rows -> new TableData(vars, rows);

        return result;
    }

    public static <T> void processExecSelect(FlowableEmitter<T> emitter, QueryExecution qex, Function<? super ResultSet, ? extends T> next) {
        Query q = qex.getQuery();
        try(QueryExecution qe = qex) {
            emitter.setCancellable(qe::abort);
            ResultSet rs = qe.execSelect();
            while(!emitter.isCancelled() && rs.hasNext()) {
                T binding = next.apply(rs);
                emitter.onNext(binding);
            }
            emitter.onComplete();
        } catch (Exception e) {
            Throwable f = HttpExceptionUtils.makeHumanFriendly(e);
            emitter.onError(new Throwable("Error executing " + q, f));
        }
    }

    public static void processExecConstructTriples(FlowableEmitter<Triple> emitter, QueryExecution qex) {
        try(QueryExecution qe = qex) {
            emitter.setCancellable(qe::abort);
            Iterator<Triple> it = qe.execConstructTriples();
            while(!emitter.isCancelled() && it.hasNext()) {
                Triple item = it.next();
                emitter.onNext(item);
            }
            emitter.onComplete();
        } catch (Exception e) {
            emitter.onError(e);
        }
    }

//
//	public static void processExecConstructTriples(SingleEmitter<ResultSet> emitter, QueryExecution qe) {
//		try {
//			emitter.setCancellable(qe::abort);
//			Iterator<Triple> = qe.execConstructTriples();
//			emitter.onSuccess(rs);
//			//emitter.onComplete();
//		} catch (Exception e) {
//			emitter.onError(e);
//		}
//	}

    public static <T> Flowable<T> execSelect(Callable<? extends QueryExecution> qes, Function<? super ResultSet, T> next) {
        Flowable<T> result = FlowableUtils.createFlowableFromResource(
                qes::call,
                QueryExecution::execSelect,
                ResultSet::hasNext,
                next,
                QueryExecution::close
            );

//        Flowable<T> result = Flowable.generate(
//                () -> {
//                    QueryExecution qe = qes.get();
//                    return new SimpleEntry<QueryExecution, ResultSet>(qe, null);
//                },
//                (state, emitter) -> {
//                    ResultSet rs = state.getValue();
//                    if(rs == null) {
////                        System.out.println("STARTED NEW RESULT SET");
//                        rs = state.getKey().execSelect();
//                        state.setValue(rs);
//                    }
//
//                    if(rs.hasNext()) {
//                        T value = next.apply(rs);
//                        emitter.onNext(value);
//                    } else {
//                        emitter.onComplete();
//                    }
//                },
//                state -> {
//                    QueryExecution qe = state.getKey();
//                    // Note consuming the result set may also already close the qe
//                    qe.close();
//                });

//        Flowable<T> result = Flowable.create(emitter -> {
//            QueryExecution qe = qes.get();
//            processExecSelect(emitter, qe, next);
//            //new Thread(() -> process(emitter, qe)).start();
//        }, BackpressureStrategy.BUFFER);

        return result;
    }

    public static Flowable<Binding> execSelectRaw(Callable<? extends QueryExecution> qes) {
        return execSelect(qes, ResultSet::nextBinding);
    }

    public static Flowable<QuerySolution> execSelect(Callable<? extends QueryExecution> qes) {
        return execSelect(qes, ResultSet::next);
    }

    public static Flowable<QuerySolution> execSelect(SparqlQueryConnection conn, String queryStr) {
        return execSelect(() -> conn.query(queryStr), ResultSet::next);
    }

    public static Flowable<QuerySolution> execSelect(SparqlQueryConnection conn, Query query) {
        return execSelect(() -> conn.query(query), ResultSet::next);
    }


    public static Flowable<Triple> execConstructTriples(SparqlQueryConnection conn, Query query) {
        return execConstructTriples(() -> conn.query(query));
    }

    public static Flowable<Triple> execConstructTriples(Callable<QueryExecution> qes) {
        Flowable<Triple> result = FlowableUtils.createFlowableFromResource(
                qes::call,
                QueryExecution::execConstructTriples,
                Iterator::hasNext,
                Iterator::next,
                QueryExecution::close
            );
        return result;
    }


    public static Flowable<Triple> constructTriples(Callable<QueryExec> qes) {
        Flowable<Triple> result = FlowableUtils.createFlowableFromResource(
                qes::call,
                QueryExec::constructTriples,
                Iterator::hasNext,
                Iterator::next,
                QueryExec::close
            );
        return result;
    }

    public static Flowable<Quad> execConstructQuads(SparqlQueryConnection conn, Query query) {
        return execConstructQuads(() -> conn.query(query));
    }

    public static Flowable<Quad> execConstructQuads(Callable<QueryExecution> qes) {
        Flowable<Quad> result = FlowableUtils.createFlowableFromResource(
                qes::call,
                QueryExecution::execConstructQuads,
                Iterator::hasNext,
                Iterator::next,
                QueryExecution::close
            );
        return result;
    }


    public static Flowable<JsonObject> execJsonItems(SparqlQueryConnection conn, Query query) {
        return execJsonItems(() -> conn.query(query));
    }

    public static Flowable<JsonObject> execJsonItems(Callable<QueryExecution> qes) {
        // Gson gson = new Gson();
        Flowable<JsonObject> result = FlowableUtils.createFlowableFromResource(
                qes::call,
                QueryExecution::execJsonItems,
                Iterator::hasNext,
                Iterator::next,
                QueryExecution::close
            ); //.map(obj -> gson.fromJson(Objects.toString(obj), JsonElement.class));
        return result;
    }
//    public static Flowable<Triple> execConstructTriples(Callable<QueryExecution> qes) {
//        Flowable<Triple> result = Flowable.create(emitter -> {
//            QueryExecution qe = qes.call();
//            processExecConstructTriples(emitter, qe);
//            //new Thread(() -> process(emitter, qe)).start();
//        }, BackpressureStrategy.BUFFER);
//
//        return result;
//    }



    public static <T> Flowable<T> select(Callable<? extends QueryExec> qes, Function<? super RowSet, T> next) {
        Flowable<T> result = FlowableUtils.createFlowableFromResource(
                qes::call,
                QueryExec::select,
                RowSet::hasNext,
                next,
                QueryExec::close
            );
        return result;
    }


//    public static Single<Range<Long>> fetchCountQuery(
//            QueryExecFactoryQuery qef,
//            Query query,
//            Long itemLimit,
//            Long rowLimit) {
//        Single<Range<Long>> result = fetchCountQueryPartition(qef, query, null, itemLimit, rowLimit);
//        return result;
//    }

    public static Flowable<Binding> select(QueryExecFactoryQuery qef, Query query) {
        return select(() -> qef.create(query));
    }

    public static Flowable<Binding> select(Callable<? extends QueryExec> qes) {
        return select(qes, RowSet::next);
    }


    public static Entry<List<Var>, Flowable<Binding>> mapToFlowable(ResultSet rs) {
        Iterator<Binding> it = new QueryIteratorResultSet(rs);
        Iterable<Binding> i = () -> it;

        List<Var> vars = VarUtils.toList(rs.getResultVars());

        Flowable<Binding> flowable = Flowable.fromIterable(i);
        Entry<List<Var>, Flowable<Binding>> result = new SimpleEntry<>(vars, flowable);
        return result;
    }

    public static Flowable<Binding> mapToBinding(ResultSet rs) {
        Entry<List<Var>, Flowable<Binding>> e = mapToFlowable(rs);
        Flowable<Binding> result = e.getValue();
        return result;
    }

//	public static Flowable<Binding> mapToBinding(ResultSet rs) {
//		Iterator<Binding> it = new IteratorResultSetBinding(rs);
//		Iterable<Binding> i = () -> it;
//		return Flowable.fromIterable(i);
//	}

    /**
     * Create a grouping function
     *
     * Usage:
     * flowable
     * 	.groupBy(createGrouper(Arrays.asList(... yourVars ...)))
     *
     * @param vars
     * @param retainNulls
     * @return
     */
    public static Function<Binding, Binding> createGrouper(Collection<Var> vars, boolean retainNulls) {
        return b -> {
            BindingBuilder groupKey = BindingFactory.builder();
            for(Var k : vars) {
                Node v = b.get(k);
                if(v != null || retainNulls) {
                    groupKey.add(k, v);
                }
            }
            return groupKey.build();
        };
    }

    public static Function<Binding, Node> createGrouper(Var var) {
        return b -> {
            Node groupKey = b.get(var);
            return groupKey;
        };
    }

//	public static Flowable<Table> groupBy(Flowable<Binding> )

    // /**
    // * Mapping that includes
    // *
    // */
    // public static Flowable<Entry<List<Var>, Binding>>(List<Var> vars, ) {
    //
    // }

//	public static void main(String[] args) {
//		// Some tests for whether timeouts actually work - so far it worked...
//		String queryStr = "CONSTRUCT { ?p a ?c } { { SELECT ?p (COUNT(DISTINCT ?s) AS ?c) { ?s ?p ?o } GROUP BY ?p } }";
//		//String queryStr = "CONSTRUCT WHERE { ?s ?p ?o . ?x ?y ?z }";
//		Query query = QueryFactory.create(queryStr);
//
//		RDFConnection conn = RDFConnectionFactory.connect("http://localhost:8890/sparql");
//		List<RDFNode> rdfNodes = SparqlRx.execPartitioned(conn, Vars.p, query, true)
//			.timeout(300, TimeUnit.MILLISECONDS)
//			.toList()
//			.blockingGet();
//
//		System.out.println(rdfNodes);
//	}

    public static void main2(String[] args) {
//		List<Entry<Integer, List<Entry<Integer, Integer>>>> list = groupByOrdered(Flowable.range(0, 10).map(i -> Maps.immutableEntry((int)(i / 3), i)),
//		e -> e.getKey())
//		.toList().blockingGet();


        Integer currentValue[] = {null};
        boolean isCancelled[] = {false};

        Flowable<Entry<Integer, List<Integer>>> list = Flowable
                .range(0, 10)
                .doOnNext(i -> currentValue[0] = i)
                .doOnCancel(() -> isCancelled[0] = true)
                .map(i -> Maps.immutableEntry((int)(i / 3), i))
                .lift(FlowableOperatorCollapseRuns.<Entry<Integer, Integer>, Integer, List<Integer>>create(CollapseRunsSpec.create(
                        Entry::getKey,
                        groupKey -> new ArrayList<>(),
                        (acc, e) -> acc.add(e.getValue()))));

        Predicate<Entry<Integer, List<Integer>>> p = e -> e.getKey().equals(1);
        list.takeUntil(p).subscribe(x -> System.out.println("Item: " + x));

        System.out.println("Value = " + currentValue[0] + ", isCancelled = " + isCancelled[0]);

//		Disposable d = list.defe
//
//		Iterator<Entry<Integer, List<Integer>>> it = list.iterator();
//		for(int i = 0; i < 2 && it.hasNext(); ++i) {
//			Entry<Integer, List<Integer>> item = it.next();
//			System.out.println("Item: " + item);
//		}
//
//
//		System.out.println("List: " + list);


        PublishProcessor<String> queue = PublishProcessor.create();
        queue.buffer(3).subscribe(x -> System.out.println("Buffer: " + x));

        for(int i = 0; i < 10; ++i) {
            String item = "item" + i;
            System.out.println("Adding " + item);
            queue.onNext(item);
        }
        queue.onComplete();


        if(true) {
            return;
        }

        for(int j = 0; j < 10; ++j) {
            int i[] = { 0 };
            System.out.println("HERE");
            execSelectRaw(() -> QueryExecutionHTTP.service("http://dbpedia.org/sparql",
                    "SELECT * { ?s a <http://dbpedia.org/ontology/Person> }"))
                            .takeUntil(b -> i[0] == 10).subscribe(x -> {
                                i[0]++;
                                System.out.println("x: " + x);

                            });
        }
        // NOTE This way, the main thread will terminate before the queries are processed
    }

//    public static Single<Number> fetchNumber(
//            SparqlQueryConnection conn,
//            Query query,
//            Var var) {
//        return fetchNumber(conn::query, query, var);
//    }


    public static Single<Number> fetchNumber(
            QueryExecFactoryQuery qef,
            Query query,
            Var var) {
        Callable<QueryExec> callable = () -> qef.create(query);
        Single<Number> result = fetchNumber(callable, var);
        return result;
    }

    public static Single<Number> fetchNumber(Callable<? extends QueryExec> queryConnSupp, Var var) {
        Single<Number> result = SparqlRx.select(queryConnSupp)
                .map(b -> BindingUtils.getNumber(b, var))
                .singleOrError()
                .onErrorResumeNext(Single::error);
                // .map(Optional::ofNullable)
                // .single(Optional.empty())
                // .map(Optional::get); // Should never be null here
        return result;
    }


//	return ReactiveSparqlUtils.execSelect(() -> qef.createQueryExecution(countQuery))
//        	.map(b -> b.get(outputVar))
//        	.map(countNode -> ((Number)countNode.getLiteralValue()).longValue())
//        	.map(count -> {
//        		boolean mayHaveMoreItems = rowLimit != null
//        				? true
//        				: itemLimit != null && count > itemLimit;
//
//                Range<Long> r = mayHaveMoreItems ? Range.atLeast(itemLimit) : Range.singleton(count);
//                return r;
//        	})
//        	.single(null);

//    public static Single<Range<Long>> fetchCountQuery(SparqlQueryConnection conn, Query query, Long itemLimit, Long rowLimit) {
//    	Single<Range<Long>> result = fetchCountQuery(conn, query, itemLimit, rowLimit);
//    	return result;
//    }


//    public static Single<Range<Long>> fetchCountQueryPartition(
//            SparqlQueryConnection conn,
//            Query query,
//            Collection<Var> partitionVars,
//            Long itemLimit,
//            Long rowLimit) {
//        return fetchCountQueryPartition(conn::query, query, partitionVars, itemLimit, rowLimit);
//    }

    public static Single<Range<Long>> fetchCountQueryPartition(
            QueryExecutionFactoryQuery qef,
            Query query,
            Collection<Var> partitionVars,
            Long itemLimit,
            Long rowLimit) {
        return fetchCountQueryPartition(
                QueryExecFactories.adapt(qef),
                query,
                partitionVars,
                itemLimit,
                rowLimit
        );
    }

    public static Single<Range<Long>> fetchCountQueryPartition(
            QueryExecFactoryQuery qef,
            Query query,
            Collection<Var> partitionVars,
            Long itemLimit,
            Long rowLimit) {

        //Var outputVar = Var.alloc("_count_"); //ConceptUtils.freshVar(concept);

        Long xitemLimit = itemLimit == null ? null : itemLimit + 1;
        Long xrowLimit = rowLimit == null ? null : rowLimit + 1;

        Entry<Var, Query> countQuery = QueryGenerationUtils.createQueryCountPartition(query, partitionVars, xitemLimit, xrowLimit);
//        System.out.println("Given: " + query);
//        System.out.println(partitionVars);
//        System.out.println("Generated count query: " + countQuery);

        Var v = countQuery.getKey();
        Query q = countQuery.getValue();

        return SparqlRx.fetchNumber(qef, q, v)
                .map(count -> SparqlRx.toRange(count.longValue(), xitemLimit, xrowLimit));
    }

    public static Single<Long> fetchBindingCount(String serviceUrl, Query query) {
        Entry<Var, Query> countQuery = QueryGenerationUtils.createQueryCount(query);
        return SparqlRx.fetchNumber(() ->
            QueryExecHTTP.newBuilder().endpoint(serviceUrl).query(countQuery.getValue()).build(),
                countQuery.getKey())
                .map(Number::longValue);
    }

//    public static Single<Range<Long>> fetchCountQuery(
//            SparqlQueryConnection conn,
//            Query query,
//            Long itemLimit,
//            Long rowLimit) {
//        Single<Range<Long>> result = fetchCountQuery(conn::query, query, itemLimit, rowLimit);
//        return result;
//    }

    public static Single<Range<Long>> fetchCountQuery(
            QueryExecutionFactoryQuery qef,
            Query query,
            Long itemLimit,
            Long rowLimit) {
        Single<Range<Long>> result = fetchCountQueryPartition(QueryExecFactories.adapt(qef), query, null, itemLimit, rowLimit);
        return result;
    }

    public static Single<Range<Long>> fetchCountQuery(
            QueryExecFactoryQuery qef,
            Query query,
            Long itemLimit,
            Long rowLimit) {
        Single<Range<Long>> result = fetchCountQueryPartition(qef, query, null, itemLimit, rowLimit);
        return result;
    }


    public static Range<Long> toRange(Long count, Long itemLimit, Long rowLimit) {
        boolean mayHaveMoreItems = rowLimit != null
                ? true
                : itemLimit != null && count > itemLimit;

        Range<Long> r = mayHaveMoreItems ? Range.atLeast(count) : Range.singleton(count);
        return r;
    }

    public static Flowable<RDFNode> execConstructGrouped(QueryExecutionFactoryQuery conn, Entry<? extends Node, Query> e) {
        return execConstructGrouped(conn, e, true);
    }

    public static Flowable<RDFNode> execConstructGrouped(QueryExecutionFactoryQuery conn, Entry<? extends Node, Query> e, boolean sortRowsByPartitionVar) {
        Node s = e.getKey();
        Query q = e.getValue();

        return execConstructGrouped(conn, q, s, sortRowsByPartitionVar);
    }

    /* Use grouped execution which aggregates over multiple rows */
    @Deprecated
    public static Flowable<RDFNode> execPartitioned(QueryExecutionFactoryQuery conn, Entry<? extends Node, Query> e) {
        return execPartitioned(conn, e, true);
    }

    /* Use grouped execution which aggregates over multiple rows */
    @Deprecated
    public static Flowable<RDFNode> execPartitioned(QueryExecutionFactoryQuery conn, Entry<? extends Node, Query> e, boolean sortRowsByPartitionVar) {
        Node s = e.getKey();
        Query q = e.getValue();

        return execPartitioned(conn, s, q, sortRowsByPartitionVar);
    }


    public static Flowable<RDFNode> execConstructGrouped(QueryExecutionFactoryQuery conn, Query query, Node s) {
        return execConstructGrouped(conn, query, s, true);
    }


    public static Flowable<RDFNode> execConstructGrouped(QueryExecutionFactoryQuery conn, Query query, Node s, boolean sortRowsByPartitionVar) {
        return execConstructGrouped(conn::createQueryExecution, query, Collections.singletonList((Var)s), s, sortRowsByPartitionVar)
                .map(Entry::getValue);
    }

//    public static Flowable<Entry<Binding, RDFNode>> execConstructGrouped(QueryExecutionFactoryQuery conn, Query query, List<Var> primaryKeyVars, Node rootNode, boolean sortRowsByPartitionVar) {
//        return execConstructGrouped(q -> conn.query(q), query, primaryKeyVars, rootNode, sortRowsByPartitionVar);
//    }

    public static Flowable<Entry<Binding, RDFNode>> execConstructGrouped(RDFDataSource dataSource, Query query, List<Var> primaryKeyVars, Node rootNode, boolean sortRowsByPartitionVar) {
        return execConstructGrouped(q -> dataSource.asQef().createQueryExecution(q), query, primaryKeyVars, rootNode, sortRowsByPartitionVar);
    }


    public static Flowable<Entry<Binding, RDFNode>> execConstructGrouped(Function<Query, QueryExecution> qeSupp, Query query, List<Var> primaryKeyVars, Node rootNode, boolean sortRowsByPartitionVar) {
        if(rootNode.isVariable() && !primaryKeyVars.contains(rootNode)) {
            throw new RuntimeException("If the root node is a variable it must be among the primary key ones");
        }

        Template template = query.getConstructTemplate();
        Query clone = preprocessQueryForPartition(query, primaryKeyVars, sortRowsByPartitionVar);

        Function<Binding, Binding> grouper = createGrouper(primaryKeyVars, false);

        Flowable<Entry<Binding, RDFNode>> result = SparqlRx
            // For future reference: If we get an empty results by using the query object, we probably have wrapped a variable with NodeValue.makeNode.
            .execSelectRaw(() -> qeSupp.apply(clone))
            //.groupBy(createGrouper(primaryKeyVars, false)::apply)
            .lift(FlowableOperatorCollapseRuns.<Binding, Binding, AccGraph2>create(CollapseRunsSpec.create(
                    grouper::apply,
                    groupKey -> new AccGraph2(template),
                    AccGraph2::accumulate)))
            .map(keyAndAcc -> {
                Binding groupKey = keyAndAcc.getKey();
                AccGraph2 accGraph = keyAndAcc.getValue();
                Map<Node, Node> bnodeMap = accGraph.getBnodeMap();

                // TODO The accumulator should manage a blank node label map which we re-use to map the rootNode

                Node effectiveRoot = rootNode.isVariable()
                        ? groupKey.get((Var)rootNode)
                        : rootNode.isBlank()
                            ? bnodeMap.get(rootNode)
                            : rootNode;

                Graph g = accGraph.getValue();
                Model m = ModelFactory.createModelForGraph(g);
                RDFNode r = m.asRDFNode(effectiveRoot);
                return Maps.immutableEntry(groupKey, r);
            });
            // Filter out null group keys; they can e.g. occur due to https://issues.apache.org/jira/browse/JENA-1487
            // .filter(group -> group.getKey() != null)
//            .map(group -> {
//                // Binding groupKey = group.getKey();
//                AccGraph acc = new AccGraph(template);
//                group.forEach(acc::accumulate);
//                Graph g = acc.getValue();
//                Model m = ModelFactory.createModelForGraph(g);
//                RDFNode r = m.asRDFNode(rootNode);
//                return r;
//            });
        return result;
    }


    /**
     * Return a SELECT query from the given query where
     * - it is ensured that all primaryKeyVars are part of the projection (if they aren't already)
     * - distinct is applied in preparation to instantiation of construct templates (where duplicates can be ignored)
     * - if sortRowsByPartitionVar is true then result bindings are sorted by the primary key vars
     *   so that bindings that belong together are consecutive
     * - In case of a construct template without variables variable free is handled
     *
     *
     * @param q
     * @param primaryKeyVars
     * @param sortRowsByPartitionVar
     * @return
     */
    public static Query preprocessQueryForPartition(Query q, List<Var> primaryKeyVars, boolean sortRowsByPartitionVar) {

        Template template = q.getConstructTemplate();
        Set<Var> projectVars = new LinkedHashSet<>();
        projectVars.addAll(primaryKeyVars);

        projectVars.addAll(QuadPatternUtils.getVarsMentioned(template.getQuads()));

        Query clone = q.cloneQuery();
        clone.setQuerySelectType();
        clone.getProject().clear();


        if(projectVars.isEmpty()) {
            // If the template is variable free then project the first variable of the query pattern
            // If the query pattern is variable free then just use the result star
            Set<Var> patternVars = SetUtils.asSet(PatternVars.vars(q.getQueryPattern()));
            if(patternVars.isEmpty()) {
                clone.setQueryResultStar(true);
            } else {
                Var v = patternVars.iterator().next();
                clone.setQueryResultStar(false);
                clone.getProject().add(v);
            }
        } else {
            clone.setQueryResultStar(false);
            clone.addProjectVars(projectVars);
        }

        clone.setDistinct(true);

        if(sortRowsByPartitionVar) {
            // TODO Check that there is no prior sort condition already
            for(Var primaryKeyVar : primaryKeyVars) {
                clone.addOrderBy(new SortCondition(primaryKeyVar, Query.ORDER_ASCENDING));
            }
        }

        logger.debug("Converted query to: " + clone);
        return clone;
    }


    /* Use grouped execution which aggregates over multiple rows */
    @Deprecated
    public static Flowable<RDFNode> execPartitioned(QueryExecutionFactoryQuery conn, Node s, Query q, boolean sortRowsByPartitionVar) {

        Template template = q.getConstructTemplate();
        Query clone = preprocessQueryForPartition(q, Collections.singletonList((Var)s), sortRowsByPartitionVar);

        Flowable<RDFNode> result = SparqlRx
                // For future reference: If we get an empty results by using the query object, we probably have wrapped a variable with NodeValue.makeNode.
                .execSelectRaw(() -> conn.createQueryExecution(clone))
                .map(b -> {
                    Graph graph = GraphFactory.createDefaultGraph();

                    // TODO Re-allocate blank nodes
                    if(template != null) {
                        Iterator<Triple> it = TemplateLib.calcTriples(template.getTriples(), Iterators.singletonIterator(b));
                        while(it.hasNext()) {
                            Triple t = it.next();
                            graph.add(t);
                        }
                    }

                    Node rootNode = s.isVariable() ? b.get((Var)s) : s;

                    Model m = ModelFactory.createModelForGraph(graph);
                    RDFNode r = m.asRDFNode(rootNode);
                    //Resource r = n.asResource();
//					Resource r = m.createResource()
//					.addProperty(RDF.predicate, m.asRDFNode(valueNode))
//					.addProperty(Vocab.facetValueCount, );
//				//m.wrapAsResource(valueNode);
//				return r;

                    return r;
                });
        return result;
    }


    public static <T extends RDFNode> Flowable<T> execConcept(Callable<QueryExecution> qeSupp, Var var, Class<T> clazz) {
        return execConcept(qeSupp, var)
            .map(rdfNode -> rdfNode.as(clazz));
    }


    public static Flowable<RDFNode> execConcept(Callable<QueryExecution> qeSupp, Var var) {
        String varName = var.getName();

        return SparqlRx.execSelect(qeSupp)
            .map(qs -> qs.get(varName));
    }

    public static Flowable<Node> execConceptRaw(Callable<QueryExecution> qeSupp, Var var) {
        return SparqlRx.execSelectRaw(qeSupp)
            .map(binding -> binding.get(var));
    }

    public static Flowable<Node> execConceptRaw(SparqlQueryConnection conn, Query query, Var var) {
        return execConceptRaw(() -> conn.query(query), var);
    }

    public static <T extends RDFNode> Flowable<T> execConcept(Function<? super Query, ? extends QueryExecution> qef, Query query, Var var, Class<T> clazz) {
        return execConcept(() -> qef.apply(query), var)
                .map(rdfNode -> rdfNode.as(clazz));
    }

    public static Flowable<Node> execConceptRaw(Function<? super Query, ? extends QueryExecution> qef, Query query, Var var) {
        return execConceptRaw(() -> qef.apply(query), var);
    }

    /** Convenience method that assumes a single projected variable */
    public static Flowable<Node> execConceptRaw(Function<? super Query, ? extends QueryExecution> qef, Query query) {
        Var var = Iterables.getOnlyElement(query.getProjectVars());
        return execConceptRaw(qef, query, var);
    }


//    public static Single<Range<Long>> fetchCountConcept(SparqlQueryConnection conn, UnaryRelation concept, Long itemLimit, Long rowLimit) {
//        return fetchCountConcept(conn::query, concept, itemLimit, rowLimit);
//    }
//
//
//    public static Single<Range<Long>> fetchCountConcept(Function<? super Query, ? extends QueryExecution> qef, Query concept, Long itemLimit, Long rowLimit) {
//
//    	fetchCountQueryPartition(qef, null, null, itemLimit, rowLimit);
//
//    	return SparqlRx.fetchNumber(qef, countQuery, outputVar)
//                .map(count -> SparqlRx.toRange(count.longValue(), xitemLimit, xrowLimit));
//    }

}
