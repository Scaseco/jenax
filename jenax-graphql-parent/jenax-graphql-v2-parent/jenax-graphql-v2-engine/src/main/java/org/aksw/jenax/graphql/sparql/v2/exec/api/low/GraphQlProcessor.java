package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AggStateGon;
import org.aksw.jenax.graphql.sparql.v2.api2.ElementGeneratorLateral;
import org.aksw.jenax.graphql.sparql.v2.api2.ElementGeneratorLateral.ElementMapping;
import org.aksw.jenax.graphql.sparql.v2.model.ElementNode;
import org.aksw.jenax.graphql.sparql.v2.rewrite.GraphQlFieldRewrite;
import org.aksw.jenax.graphql.sparql.v2.rewrite.GraphQlToSparqlConverterBase;
import org.aksw.jenax.graphql.sparql.v2.rewrite.RewriteResult;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformAssignGlobalIds;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformExpandShorthands;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformPullDebug;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformPullNdJson;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformPullOrdered;
import org.aksw.jenax.graphql.sparql.v2.rewrite.TransformPullPretty;
import org.aksw.jenax.graphql.sparql.v2.util.ElementUtils;
import org.aksw.jenax.graphql.util.GraphQlUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementLateral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.language.AstPrinter;
import graphql.language.Document;
import graphql.language.NodeTraverser;

public class GraphQlProcessor<K> {
    private static final Logger logger = LoggerFactory.getLogger(GraphQlProcessor.class);

    /** The preprocessed document. This is not the original document. */
    protected Document preprocessedDocument;

    /** The processor for the query operation (across all top-level fields). */
    protected GraphQlFieldProcessor<K> queryProcessor;

    /* Processors for the top-level fields. */
    protected Map<String, Integer> nameToIndex;
    protected List<GraphQlFieldProcessor<K>> fieldProcessors;

    public GraphQlProcessor(Document preprocessedDocument, GraphQlFieldProcessor<K> queryProcessor, Map<String, Integer> nameToIndex, List<GraphQlFieldProcessor<K>> fieldProcessors) {
        super();
        this.preprocessedDocument = preprocessedDocument;
        this.queryProcessor = queryProcessor;
        this.nameToIndex = nameToIndex;
        this.fieldProcessors = fieldProcessors;
    }

    // @Override
    public Set<String> getDataProviderNames() {
        return nameToIndex.keySet();
    }

    public Document getPreprocessedDocument() {
        return preprocessedDocument;
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

    public static <K> GraphQlProcessor<K> of(Document document, Map<String, Node> assignments, GraphQlToSparqlConverterBase<K> graphqlToSparqlConverter) {
        Document b = GraphQlUtils.applyTransform(document, new TransformExpandShorthands());

        Document c = b;
        c = GraphQlUtils.applyTransform(c, new TransformPullDebug());
        c = GraphQlUtils.applyTransform(c, new TransformPullPretty());
        c = GraphQlUtils.applyTransform(c, new TransformPullNdJson());
        c = GraphQlUtils.applyTransform(c, new TransformPullOrdered());

        Document preprocessedDoc = GraphQlUtils.applyTransform(c, TransformAssignGlobalIds.of("state_", 0));

        if (logger.isDebugEnabled()) {
            logger.debug("Preprocessing GraphQl document: " + AstPrinter.printAst(preprocessedDoc));
        }
        System.err.println("Preprocessing GraphQl document: " + AstPrinter.printAst(preprocessedDoc));

        boolean globalOrderBy = GraphQlUtils.hasQueryDirective(preprocessedDoc, "ordered");

        NodeTraverser nodeTraverser = new NodeTraverser(); //rootVars, Node::getChildren);
        // GraphQlToSparqlConverterBase<K> graphqlToSparqlConverter = converterFactory.create();

        // RewriteResult rewriteResult = (RewriteResult)nodeTraverser.depthFirst(graphqlToSparqlConverter, preprocessedDoc);
        nodeTraverser.depthFirst(graphqlToSparqlConverter, preprocessedDoc);
        RewriteResult<K> rewriteResult = graphqlToSparqlConverter.getRewriteResult();

        // The processor for the whole query operation (not just an individual top level field)
        GraphQlFieldProcessor<K> queryProcessor = makeProcessor(globalOrderBy, "root", rewriteResult.root());

        // Set up processors for the individual top level fields
        List<GraphQlFieldProcessor<K>> fieldProcessors = new ArrayList<>(rewriteResult.map().size());
        Map<String, Integer> nameToIndex = new LinkedHashMap<>();
        int i = 0;
        for (Entry<String, GraphQlFieldRewrite<K>> entry : rewriteResult.map().entrySet()) {

            String name = entry.getKey();
            GraphQlFieldProcessor<K> fieldProcessor = makeProcessor(globalOrderBy, name, entry.getValue());

            fieldProcessors.add(fieldProcessor);
            nameToIndex.put(name, i);
            ++i;
            // GraphQlDataProviderExec<P_Path0, Node> result = GraphQlExecUtils2.exec(elementNode, agg, query -> queryExecBuilder.query(query).build());
        }

        GraphQlProcessor<K> result = new GraphQlProcessor<>(preprocessedDoc, queryProcessor, nameToIndex, fieldProcessors);

        // Somewhat ugly post processing to set the overall processor on every field processor
        ((GraphQlFieldProcessorImpl<K>)queryProcessor).setGraphQlProcessor(result);
        for (GraphQlFieldProcessor<K> fieldProcessor : fieldProcessors) {
            ((GraphQlFieldProcessorImpl<K>)fieldProcessor).setGraphQlProcessor(result);
        }

        return result;
    }

    private static <K> GraphQlFieldProcessor<K> makeProcessor(boolean globalOrderBy, String name, GraphQlFieldRewrite<K> fieldRewrite) {
        ElementNode elementNode = fieldRewrite.rootElementNode();
        boolean isSingle = fieldRewrite.isSingle();

        Var stateVar = Var.alloc("state");
        Node rootStateId = NodeFactory.createLiteralString(elementNode.getIdentifier());

        // Important: stateIds are jena Nodes (not java objects)!

        ElementMapping eltMap = ElementGeneratorLateral.toLateral(globalOrderBy, elementNode, stateVar);
        Element elt = eltMap.element();
        Map<?, Map<Var, Var>> stateVarMap = eltMap.stateVarMap();

        // Root var map: original var -> renamed var (so that access by original var is possibleg)
        Map<Var, Var> rootVarMap = stateVarMap.get(rootStateId);

        // Remove needless top-level groups-of-one and lateral
        // XXX Should toLateral() already handle this?
        elt = ElementUtils.recursivelyUnnestGroupsOfOne(elt);
        if (elt instanceof ElementLateral l) {
            elt = l.getLateralElement();
        }

        List<Var> projectVars = stateVarMap.values().stream()
                .flatMap(varMap -> varMap.values().stream())
                .distinct()
                .toList();

        Query query = new Query();
        query.setQuerySelectType();
        query.setQueryResultStar(false);
        query.addProjectVars(projectVars);
        query.setQueryPattern(elt);

        if (globalOrderBy) {
            eltMap.sortConditions().forEach(query::addOrderBy);
        }

        AggStateGon<Binding, FunctionEnv, K, Node> agg = fieldRewrite.rootAggBuilder().newAggregator();

        QueryMapping<K> queryMapping = new QueryMapping<>(name, stateVar, rootStateId, query, stateVarMap, agg, isSingle, fieldRewrite);
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
