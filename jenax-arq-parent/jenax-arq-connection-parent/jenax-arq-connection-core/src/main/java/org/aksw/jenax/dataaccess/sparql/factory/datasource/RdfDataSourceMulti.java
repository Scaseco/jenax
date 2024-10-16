package org.aksw.jenax.dataaccess.sparql.factory.datasource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.jenax.arq.util.binding.QueryIterOverQueryExec;
import org.aksw.jenax.arq.util.node.NodeUtils;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryBase;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLinkModular;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecAdapter;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementService;

/*
 * Dealing with aggregate functions over partitions:
 *
 *
 * MIN/MAX/SUM: This can be handled correctly by just taking the overall MIN of all member's MIN.
 *   SELECT (MAX(?member_x) AS ?x) { ... SELECT MAX(?foo) AS ?member_x}}
 *
 * DISTINCT: Push distinct to the source and take the overall distinct. (same as min/max)
 *
 * COUNT: A heuristic would be to push the COUNT to the member and then SUM up the result. But this would just yield a rough estimate.
 *
 * AVG := SUM / COUNT
 * GROUPCONCAT
 *
 *
 * So there are 2 different ways for multi-source queries:
 * - Transparent: The data source forwards the query pattern to the sources and accumulates the result locally.
 *   + Aggregations would work mostly as expected (triples/bindings may have duplicates across the sources)
 *   - Not good for analytic queries.
 *
 * - Explicit: Queries need to be written in a source-aware fashion: e.g.
 *   SELECT * {
 *     SERVICE <source:> { # Repeat pattern for all sources
 *       BIND(<env:source AS ?source) # Special constant that will be assigned to the source variable
 *     }
 *   }
 *
 * Another approach could be to design many components to be multi-source aware from the start.
 * But that would mean that each rdfTerm would become a pair (source, orginalValue).
 *
 *
 */


record MultiRequestElt(
    /** The node to use for the service. If a data source is given, then a service registry is set up that executes the request on the data source. */
    Node serviceNode,
    Element element,

    /** The data source on which to execute the element. */
    RdfDataSource dataSource,

    /** A binding to merge into all bindings obtained from the data source */
    @Deprecated /** It is better to substitute special rdf terms with values from an environment (e.g. env://SOURCE),
    rather than injecting variables that were not asked for in the original query. */
    Binding binding
) {}


class MultiRequestBuilder {
    protected List<MultiRequestElt> members = new ArrayList<>();

    /**
     *
     * SELECT * {
     *     {
     *       SERVICE <member1:> {
     *         element1
     *       }
     *       BIND(bindingValue1 AS ?bindingVar1) ...
     *     }
     *   UNION
     *     ...
     *   UNION
     *     {
     *       SERVICE <memberN:> {
     *         elementN
     *       }
     *       BIND(bindingValue1 AS ?bindingVar1) ...
     *     }
     * }
     *
     */

    public static Node allocate(Node base, Predicate<Node> alreadySeen) {
        String lex = NodeUtils.getUnquotedForm(base);
        Node result = null;
        for (int i = 0; i < Integer.MAX_VALUE; ++i) {
            String str = lex + "_" + i;
            result = base.isURI() ? NodeFactory.createURI(str) : NodeFactory.createLiteralString(str);

            if (alreadySeen.test(result)) {
                continue;
            }

            break;
        }
        return result;
    }

    public void add(Node serviceNode, Element element, RdfDataSource dataSource, Binding binding) {
        members.add(new MultiRequestElt(serviceNode, element, dataSource, binding));
    }

//    class MultiReq {
//        protected Map<Node, RdfDataSource> nodeToSource;
//        protected List<Element> elements;
//    }


    /**
     * Create a query execution.
     * Internally creates a query that federates via SERVICE.
     *
     *
     * @param eltToQuery
     * @return
     */
    public QueryExec build(Function<Element, Query> eltToQuery) {
        // Assign each data source a service Node
        Set<Node> seenDataSourceNodes = new HashSet<>();
        IdentityHashMap<RdfDataSource, Node> sourceToNode = new IdentityHashMap<>();
        for (int i = 0; i < members.size(); ++i) {
            MultiRequestElt member = members.get(i);
            RdfDataSource dataSource = member.dataSource();
            if (dataSource != null) {
                Node sourceNode = sourceToNode.get(dataSource);
                if (sourceNode == null) {
                    Node serviceNode = member.serviceNode();
                    if (serviceNode == null) {
                        serviceNode = NodeFactory.createURI("urn:service:id");
                    }
                    sourceNode = allocate(serviceNode, seenDataSourceNodes::contains);
                    sourceToNode.put(dataSource, sourceNode);
                    seenDataSourceNodes.add(sourceNode);
                }
            }
        }
        Map<Node, RdfDataSource> nodeToSource = sourceToNode.entrySet().stream().collect(Collectors.toMap(Entry::getValue, Entry::getKey));

        List<Element> elts = new ArrayList<>();
        for (MultiRequestElt member : members) {
            Binding b = member.binding();
            Element elt = member.element();
            if (b != null) {
                ElementGroup group = new ElementGroup();
                ElementUtils.copyElements(group, elt);
                ElementUtils.addBinding(group, b);
                elt = group;
            }

            Node dataSourceServiceNode = sourceToNode.getOrDefault(member.dataSource(), member.serviceNode());
            Node memberServiceNode = Optional.ofNullable(member.serviceNode()).orElse(dataSourceServiceNode);

            // Replace env:SOURCE iris (if any) with the respective service
            Node SOURCE = NodeFactory.createURI("env://SOURCE");
//            elt = ElementUtils.applyNodeTransform(elt, node -> NodeEnvsubst.subst(node, k -> {
//                return "SOURCE".equals(k) ? serviceNode : k;
//            }));
            elt = ElementUtils.applyNodeTransform(elt, node -> SOURCE.equals(node) ? memberServiceNode : node);

            elts.add(new ElementService(dataSourceServiceNode, elt, false));
        }

        Element union = ElementUtils.unionIfNeeded(elts);

        Query query = eltToQuery.apply(union); // QueryUtils.elementToQuery(union);

        ServiceExecutorRegistry reg = ServiceExecutorRegistry.createFrom(null);
        reg.addBulkLink((opService, input, execCxt, chain) -> {
            Node serviceNode = opService.getService();

            RdfDataSource dataSource = nodeToSource.get(serviceNode);
            QueryIterator r;
            if (dataSource != null) {
                Query qq = OpAsQuery.asQuery(opService.getSubOp());
                QueryExec qe = QueryExecAdapter.adapt(dataSource.asQef().createQueryExecution(qq));
                r = new QueryIterOverQueryExec(execCxt, qe);
            } else {
                r = chain.createExecution(opService, input, execCxt);
            }
            return r;
        });


        // XXX Push DISTINCT and GROUP BY to the source if the SOURCE variable is involved
        // Evaluator<CBinding> evaluator = new EvaluationOfConstraints();
        // CBinding cbinding = evaluator.evalOp(op, CBindingMap.create());
        // System.out.println(cbinding);

        DatasetGraph proxyDataset = DatasetGraphFactory.create();
        ServiceExecutorRegistry.set(proxyDataset.getContext(), reg);
        return QueryExec.newBuilder().dataset(proxyDataset).query(query).build();
    }
}

public class RdfDataSourceMulti
    implements RdfDataSource
{
    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        protected Map<Node, RdfDataSource> dataSources = new LinkedHashMap<>();

        public Builder add(RdfDataSource dataSource) {
            add(NodeFactory.createURI("urn:service:" + System.identityHashCode(dataSource)), dataSource);
            return this;
        }

        public Builder add(String dataSourceIri, RdfDataSource dataSource) {
            add(NodeFactory.createURI(dataSourceIri), dataSource);
            return this;
        }

        public Builder add(Node dataSourceNode, RdfDataSource dataSource) {
            dataSources.put(dataSourceNode, dataSource);
            return this;
        }

        public RdfDataSourceMulti build() {
            Map<Node, RdfDataSource> map = new LinkedHashMap<>(dataSources);
            return new RdfDataSourceMulti(map);
        }
    }

    protected Map<Node, RdfDataSource> dataSources = new LinkedHashMap<>();

    public RdfDataSourceMulti() {
        super();
    }

    protected RdfDataSourceMulti(Map<Node, RdfDataSource> dataSources) {
        super();
        this.dataSources = dataSources;
    }

    public Map<Node, RdfDataSource> getDataSources() {
        return dataSources;
    }

    @Override
    public RDFConnection getConnection() {
        return RDFConnectionAdapter.adapt(new RDFLinkModular(new LinkSparqlQueryBase() {
            @Override
            public QueryExecBuilder newQuery() {
                return new QueryExecBuilderCustomBase<>() {
                    @Override
                    public QueryExec build() {
                        Query q = this.getParsedQueryCopy();
                        Element elt = q.getQueryPattern();
                        // Element elt = new ElementSubQuery(q);
                        MultiRequestBuilder builder = new MultiRequestBuilder();

                        // for (RdfDataSource dataSource : dataSources) {
                        dataSources.forEach((node, dataSource) ->
                            builder.add(node, elt, dataSource, null)
                        );

                        QueryExec r = builder.build(e -> {
                            q.setQueryPattern(e);
                            System.err.println("Built query: " + q);
                            return q;
                        });
                        return r;
                    }
                };
            }
            @Override
            public void close() {
            }

            @Override
            public Transactional getDelegate() {
                return null;
            }
        },
        null,
        null));
    }

    public static void main(String[] args) {
        RdfDataSource ds1 = () -> RDFConnectionRemote.newBuilder().destination("http://maven.aksw.org/sparql").build();
        RdfDataSource ds2 = () -> RDFConnectionRemote.newBuilder().destination("http://linkedgeodata.org/sparql").build();
        RdfDataSource ds3 = () -> RDFConnectionRemote.newBuilder().destination("https://staging.databus.dbpedia.org/repo/sparql").build();

        // RdfDataSource dsRaw = new RdfDataSourceMulti(Arrays.asList(ds1, ds2));
        RdfDataSource dsRaw = RdfDataSourceMulti.newBuilder()
                .add("http://maven.aksw.org/sparql", ds1)
                .add("http://linkedgeodata.org/sparql", ds2)
                .add("urn://p.p.p.org/sparql", ds3)
                .build()
                ;

        RdfDataSource ds = dsRaw; //dsRaw.decorate(RdfDataSourceTransforms.)

        // XX Future optimization could detect grouping by source, so that aggregation is pushed
        // to each source individually
        String queryStrX = """
                SELECT * {
                  { SELECT ?t (COUNT(*) AS ?c) {
                    GRAPH ?g {
                      ?s a ?t .
                      FILTER(?t IN (
                        <http://www.w3.org/2000/01/rdf-schema#Class>,
                        <http://www.w3.org/2002/07/owl#Class>,
                        <http://www.w3.org/ns/dcat#Dataset>))
                      ?s ?p ?o
                     }
                   } GROUP BY ?t }
                   BIND(<env://SOURCE> AS ?source)
                 }
                 ORDER BY ?source ?t
            """;

        String queryStr = """
            SELECT ?source ?t (COUNT(*) AS ?c) {
              GRAPH ?g {
                ?s a ?t .
                FILTER(?t IN (
                  <http://www.w3.org/2000/01/rdf-schema#Class>,
                  <http://www.w3.org/2002/07/owl#Class>,
                  <http://www.w3.org/ns/dcat#Dataset>))
                ?s ?p ?o
               }
               BIND(<env://SOURCE> AS ?source)
              } GROUP BY ?source ?t
             ORDER BY ?source ?t
            """;
        // String queryStr = "SELECT (COUNT(*) AS ?c) { GRAPH ?g { ?s a <http://www.w3.org/2000/01/rdf-schema#Class> ; ?p ?o } } LIMIT 10";


        // Op op = Algebra.compile(QueryFactory.create(queryStr));

        try (QueryExecution qe = ds.asQef().createQueryExecution(queryStr)) {
            System.out.println(ResultSetFormatter.asText(qe.execSelect()));
        }

    }
}
