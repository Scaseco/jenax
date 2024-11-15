package org.aksw.jenax.dataaccess.sparql.polyfill.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtuosoSubstr;
import org.aksw.jena_sparql_api.algebra.transform.TransformExistsToOptional;
import org.aksw.jena_sparql_api.algebra.transform.TransformExpandAggCountDistinct;
import org.aksw.jena_sparql_api.algebra.transform.TransformFactorizeTableColumnsToExtend;
import org.aksw.jena_sparql_api.algebra.transform.TransformOpDatasetNamesToOpGraph;
import org.aksw.jena_sparql_api.algebra.transform.TransformRedundantFilterRemoval;
import org.aksw.jena_sparql_api.algebra.transform.TransformRedundantProjectionRemoval;
import org.aksw.jenax.arq.util.expr.FunctionUtils;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionUtils;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSourceTransforms;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.dataaccess.sparql.polyfill.detector.MainCliSparqlPolyfillModel;
import org.aksw.jenax.dataaccess.sparql.polyfill.detector.PolyfillDetector;
import org.aksw.jenax.stmt.core.SparqlStmtMgr;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.service.ServiceExecutorRegistry;

import com.google.common.collect.Iterables;

/**
 * Detect (missing) features of a sparql endpoint and recommend a sequence of RdfDataSourceTransform
 * instances to polyfill those.
 *
 * This class should eventually be improved or extended into a more general
 * SPARQL endpoint feature detector.
 */
public class RdfDataSourcePolyfill {

    public static List<Suggestion<String>> suggestPolyfills(RdfDataSource rdfDataSource) {
        Model model = ModelFactory.createDefaultModel();
        MainCliSparqlPolyfillModel.initDefaultSuggestions(model);

        // Wrap the datasource with a cache for polyfill detection
        // The cache stores all query results in-memory
        RdfDataSource cachedDataSource = rdfDataSource.decorate(RdfDataSourceTransforms.simpleCache());
        // RdfDataSource cachedDataSource = rdfDataSource;

        PolyfillDetector detector = new PolyfillDetector();
        detector.load(model);
        List<Suggestion<String>> result = detector.detect(cachedDataSource);
        return result;
    }

    public static List<Suggestion<String>> suggestPolyfillsOld(RdfDataSource rdfDataSource) {
        List<Suggestion<String>> result = null;
        String profile = RdfDataSources.compute(rdfDataSource, RdfDataSourcePolyfill::detectProfile);

        if (profile != null && profile.equals("virtuoso")) {
            result = virtuosoProfile();
        }

        if (result == null) {
            result = new ArrayList<>(); // List.of();
        }

        result.add(Suggestion.of("Generic - EXISTS as OPTIONAL", "Rewrite EXISTS conditions using OPTIONAL", TransformExistsToOptional.class.getName()));

        return result;
    }

    public static List<Suggestion<String>> virtuosoProfile() {
        List<Suggestion<String>> result = new ArrayList<>();
        // result.add(Suggestion.of("Generic - Optimize nested queries", "Pull up slice and remove needless projections", TransformOptimizeSubQueries.class.getName()));
        result.add(Suggestion.of("Generic - LATERAL", "Client-side execution of LATERAL", RdfDataSourceWithLocalLateral.class.getName()));
        result.add(Suggestion.of("Virtuoso - Rewrite empty table", "Rewrite VALUES blocks with empty bindings", TransformFactorizeTableColumnsToExtend.class.getName()));
        result.add(Suggestion.of("Virtuoso - Rephrase COUNT(DISTINCT ?x)", "Rewrite as COUNT(*) over a sub query using DISTINCT ?x", TransformExpandAggCountDistinct.class.getName()));
        result.add(Suggestion.of("Virtuoso - Remove redundant filters", "Some versions of Virtuoso raise errors for BIND('foo' AS ?x) FILTER(?x = 'foo')", TransformRedundantFilterRemoval.class.getName()));
        result.add(Suggestion.of("Virtuoso - OpDatasetNames to OpGraph", "Rewrite Graph ?g {} to Graph ?g { ?s ?p ?o }", TransformOpDatasetNamesToOpGraph.class.getName()));
        result.add(Suggestion.of("Virtuoso - Fix SubStr", "Transform substr expressions to handle the case where the requested length is greater than the string's remaining length", ExprTransformVirtuosoSubstr.class.getName()));
        result.add(Suggestion.of("Generic - Remove redundant projections", "", TransformRedundantProjectionRemoval.class.getName()));
        return result;
    }

    public static String detectProfile(RDFConnection conn) {
        String result = null;
        // if ("auto".equalsIgnoreCase(givenProfileName) && transformer == null) {
        // A helper dataset against which the probing query is run.
        // The dataset's context gets special service handler
        Dataset probeDs = DatasetFactory.create();
        ServiceExecutorRegistry registry = new ServiceExecutorRegistry();
        registry.addSingleLink((opExec, opOrig, binding, execCxt, chain) -> {
            QueryIterator r;
            if (opExec.getService().getURI().equals("env://REMOTE")) {
                // try {
                // These requests materialize the result set, so the connection should be idle
                // once execService returns
                    r = RDFConnectionUtils.execService(binding, execCxt, opExec, conn, false, true);
                    // RDFLinkAdapter.adapt(base).query(query).sel
                    // r = new QueryIteratorResultSet(base.query(query).execSelect());
//                    } catch (Exception e) {
//                        logger.warn("Probing failed", e);
//                    }
            } else {
                r = chain.createExecution(opExec, opOrig, binding, execCxt);
            }
            return r;
        });
        ServiceExecutorRegistry.set(probeDs.getContext(), registry);

        // Disable warnings about unknown functions while probing
        FunctionUtils.runWithDisabledWarnOnUnknownFunction(() -> {
            SparqlStmtMgr.execSparql(probeDs, "probe-endpoint-dbms.sparql");
        });

        Property dbmsShortName = ResourceFactory.createProperty("http://www.example.org/dbmsShortName");

        Model report = probeDs.getDefaultModel();
        List<String> nodes = report.listObjectsOfProperty(dbmsShortName)
            .mapWith(n -> n.isLiteral() ? Objects.toString(n.asLiteral().getValue()) : null)
            .toList();
        String first = Iterables.getFirst(nodes, null);

        if(first != null) {
            result = first;
        }
        return result;
    }
}
