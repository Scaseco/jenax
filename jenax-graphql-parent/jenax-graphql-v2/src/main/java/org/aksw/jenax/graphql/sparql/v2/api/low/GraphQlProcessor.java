package org.aksw.jenax.graphql.sparql.v2.api.low;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.api.low.GraphQlFieldExecBuilderImpl.QueryMapping;
import org.aksw.jenax.graphql.sparql.v2.api2.ElementGeneratorLateral;
import org.aksw.jenax.graphql.sparql.v2.api2.ElementGeneratorLateral.ElementMapping;
import org.aksw.jenax.graphql.sparql.v2.api2.QueryUtils;
import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;
import org.aksw.jenax.graphql.sparql.v2.rewrite.GraphQlToSparqlConverterBase;
import org.aksw.jenax.graphql.sparql.v2.rewrite.RewriteResult;
import org.aksw.jenax.graphql.sparql.v2.rewrite.RewriteResult.SingleResult;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformAssignGlobalIds;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformExpandShorthands;
import org.aksw.jenax.graphql.sparql.v2.util.GraphQlUtils;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.syntax.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.language.AstPrinter;
import graphql.language.Document;
import graphql.language.NodeTraverser;

public class GraphQlProcessor<K> {
    private static final Logger logger = LoggerFactory.getLogger(GraphQlProcessor.class);

    /** The processor for the query operation (across all top-level fields). */
    protected GraphQlFieldProcessor<K> queryProcessor;

    /* Processors for the top-level fields. */
    protected Map<String, Integer> nameToIndex;
    protected List<GraphQlFieldProcessor<K>> fieldProcessors;

    public GraphQlProcessor(GraphQlFieldProcessor<K> queryProcessor, Map<String, Integer> nameToIndex, List<GraphQlFieldProcessor<K>> fieldProcessors) {
        super();
        this.queryProcessor = queryProcessor;
        this.nameToIndex = nameToIndex;
        this.fieldProcessors = fieldProcessors;
    }

    // @Override
    public Set<String> getDataProviderNames() {
        return nameToIndex.keySet();
    }

    public GraphQlFieldProcessor<K> getOnlyFieldProcessor() {
        if (fieldProcessors.size() != 1) {
            // FIXME Use better exception
            throw new IllegalArgumentException("newOnlyExecFactory can only be used with a single query mapping. Mapping names: " + nameToIndex.keySet());
        }
        return getFieldProcessor(0);
    }

    // @Override
    public GraphQlFieldProcessor<K> getFieldProcessor(String name) {
        Integer index  = nameToIndex.get(name);
        if (index == null) {
            throw new NoSuchElementException("There is no data provider with name: " + name);
        }
        return getFieldProcessor(index);
    }

    public GraphQlFieldProcessor<K> getFieldProcessor(int index) {
        GraphQlFieldProcessor<K> result  = fieldProcessors.get(index);
        return result;
    }

    public static <K> GraphQlProcessor<K> of(Document document, Map<String, Node> assignments, Creator<? extends GraphQlToSparqlConverterBase<K>> converterFactory) {
        Document b = GraphQlUtils.applyTransform(document, new TransformExpandShorthands());
        Document preprocessedDoc = GraphQlUtils.applyTransform(b, TransformAssignGlobalIds.of("state_", 0));

        if (logger.isDebugEnabled()) {
            logger.debug("Preprocessing GraphQl document: " + AstPrinter.printAst(preprocessedDoc));
        }

        NodeTraverser nodeTraverser = new NodeTraverser(); //rootVars, Node::getChildren);
        GraphQlToSparqlConverterBase<K> graphqlToSparqlConverter = converterFactory.create();

        // RewriteResult rewriteResult = (RewriteResult)nodeTraverser.depthFirst(graphqlToSparqlConverter, preprocessedDoc);
        nodeTraverser.depthFirst(graphqlToSparqlConverter, preprocessedDoc);
        RewriteResult<K> rewriteResult = graphqlToSparqlConverter.getRewriteResult();

        // The processor for the whole query operation (not just an individual top level field)
        GraphQlFieldProcessor<K> queryProcessor = makeProcessor("root", rewriteResult.root());

        // Set up processors for the individual top level fields
        List<GraphQlFieldProcessor<K>> fieldProcessors = new ArrayList<>(rewriteResult.map().size());
        Map<String, Integer> nameToIndex = new LinkedHashMap<>();
        int i = 0;
        for (Entry<String, SingleResult<K>> entry : rewriteResult.map().entrySet()) {

            String name = entry.getKey();
            GraphQlFieldProcessor<K> fieldProcessor = makeProcessor(name, entry.getValue());

            fieldProcessors.add(fieldProcessor);
            nameToIndex.put(name, i);
            ++i;
            // GraphQlDataProviderExec<P_Path0, Node> result = GraphQlExecUtils2.exec(elementNode, agg, query -> queryExecBuilder.query(query).build());
        }

        GraphQlProcessor<K> result = new GraphQlProcessor<>(queryProcessor, nameToIndex, fieldProcessors);
        return result;
    }

    private static <K> GraphQlFieldProcessor<K> makeProcessor(String name,
            SingleResult<K> single) {
        ElementNode elementNode = single.rootElementNode();
        boolean isSingle = single.isSingle();

        Var stateVar = Var.alloc("state");
        Node rootStateId = NodeFactory.createLiteralString(elementNode.getIdentifier());

        // Important: stateIds are handled as jena Nodes!

        ElementMapping eltMap = ElementGeneratorLateral.toLateral(elementNode, stateVar);
        Element elt = eltMap.element();
        Map<?, Map<Var, Var>> stateVarMap = eltMap.stateVarMap();

        Query query = QueryUtils.elementToQuery(elt);
        AggStateGon<Binding, FunctionEnv, K, Node> agg = single.rootAggBuilder().newAggregator();

        QueryMapping<K> queryMapping = new QueryMapping<>(name, stateVar, rootStateId, query, stateVarMap, agg, isSingle);
        GraphQlFieldProcessor<K> fieldProcessor = new GraphQlFieldProcessorImpl<>(name, queryMapping);
        return fieldProcessor;
    }

    public GraphQlFieldProcessor<K> getQueryProcessor() {
        return queryProcessor;
    }

    /** Short hand; abstracts from the query processor indirection. */
    public GraphQlFieldExecBuilder<K> newExecBuilder() {
        return getQueryProcessor().newExecBuilder();
    }
}
