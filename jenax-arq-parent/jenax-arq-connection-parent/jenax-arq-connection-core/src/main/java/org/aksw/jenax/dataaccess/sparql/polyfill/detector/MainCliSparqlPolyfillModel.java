package org.aksw.jenax.dataaccess.sparql.polyfill.detector;

import java.util.List;

import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtuosoSubstr;
import org.aksw.jena_sparql_api.algebra.transform.TransformExistsToOptional;
import org.aksw.jena_sparql_api.algebra.transform.TransformExpandAggCountDistinct;
import org.aksw.jena_sparql_api.algebra.transform.TransformFactorizeTableColumnsToExtend;
import org.aksw.jena_sparql_api.algebra.transform.TransformOpDatasetNamesToOpGraph;
import org.aksw.jena_sparql_api.algebra.transform.TransformRedundantFilterRemoval;
import org.aksw.jena_sparql_api.algebra.transform.TransformRedundantProjectionRemoval;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourcePolyfill;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.RdfDataSourceWithLocalLateral;
import org.aksw.jenax.dataaccess.sparql.polyfill.datasource.Suggestion;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillCondition;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillConditionQuery;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillRewriteJava;
import org.aksw.jenax.model.polyfill.domain.api.PolyfillSuggestionRule;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDFS;

public class MainCliSparqlPolyfillModel {
    public static void main(String[] args) {
        String url = "http://dbpedia.org/sparql";
        // String url = "http://maven.aksw.org/sparql";
        // String url = "http://localhost:9988/sparql";

        RdfDataSource dataSource = () -> RDFConnectionRemote.newBuilder()
                .destination(url).build();

        List<Suggestion<String>> suggestions = RdfDataSourcePolyfill.suggestPolyfills(dataSource);
        for (Suggestion<String> suggestion : suggestions) {
            System.out.println(suggestion);
        }

//        Model model = ModelFactory.createDefaultModel();
//        initDefaultSuggestions(model);
    }

    public static void initDefaultSuggestions(Model model) {
        model.setNsPrefix("norse", "https://w3id.org/aksw/norse#");
        model.setNsPrefix("rdfs", RDFS.uri);

        model.createResource().as(PolyfillSuggestionRule.class)
            .setLabel("Generic - LATERAL")
            .setComment("Client-side execution of LATERAL")
            .setCondition(model.createResource().as(PolyfillConditionQuery.class)
                .setQueryString("PREFIX : <http://www.example.org/polyfill/lateral/> SELECT * { :s :p ?o . LATERAL { ?o :p :o } }"))
            .setSuggestion(model.createResource().as(PolyfillRewriteJava.class)
                .setJavaClass(RdfDataSourceWithLocalLateral.class.getName()));

        model.createResource().as(PolyfillSuggestionRule.class)
            .setLabel("Generic - EXISTS as OPTIONAL")
            .setComment("Rewrite EXISTS conditions using OPTIONAL")
            .setCondition(model.createResource().as(PolyfillConditionQuery.class)
                .setQueryString("PREFIX : <http://www.example.org/polyfill/exists-to-optional/> SELECT * { :s :p ?o . FILTER EXISTS { ?o :p :o } }"))
            .setSuggestion(model.createResource().as(PolyfillRewriteJava.class)
                .setJavaClass(TransformExistsToOptional.class.getName()));


        model.createResource().as(PolyfillSuggestionRule.class)
            .setLabel("Generic - Remove redundant projections")
            //.setComment()
            .setLevel(10100)
            .setSuggestion(model.createResource().as(PolyfillRewriteJava.class)
                .setJavaClass(TransformRedundantProjectionRemoval.class.getName()));


        PolyfillCondition virtuoso = model.createResource().as(PolyfillConditionQuery.class)
                .setMatchOnNonEmptyResult(true)
                .setQueryString("""
                PREFIX bif: <bif:>
                SELECT * {
                  BIND(bif:sys_stat('st_dbms_name') AS ?dbmsRawName)
                  BIND(bif:sys_stat('st_dbms_ver') AS ?dbmsVersion)
                  FILTER(BOUND(?dbmsRawName))
                }
                """);

        model.createResource().as(PolyfillSuggestionRule.class)
            .setLabel("Virtuoso - Rewrite empty table")
            .setComment("Rewrite VALUES blocks with empty bindings")
            // TODO Use this test query: SELECT * { VALUES () { () () () } BIND (<urn:x> AS ?x) }
            .setCondition(virtuoso)
            .setLevel(10000)
            .setSuggestion(model.createResource().as(PolyfillRewriteJava.class)
                .setJavaClass(TransformFactorizeTableColumnsToExtend.class.getName()));

        model.createResource().as(PolyfillSuggestionRule.class)
            .setLabel("Virtuoso - Rephrase COUNT(DISTINCT ?x)")
            .setComment("Rewrite as COUNT(*) over a sub query using DISTINCT ?x")
            .setCondition(virtuoso)
            .setLevel(10100)
            .setSuggestion(model.createResource().as(PolyfillRewriteJava.class)
                .setJavaClass(TransformExpandAggCountDistinct.class.getName()));

        model.createResource().as(PolyfillSuggestionRule.class)
            .setLabel("Virtuoso - Remove redundant filters")
            .setComment("Some versions of Virtuoso raise errors for BIND('foo' AS ?x) FILTER(?x = 'foo')")
            .setCondition(virtuoso)
            .setLevel(10100)
            .setSuggestion(model.createResource().as(PolyfillRewriteJava.class)
                .setJavaClass(TransformRedundantFilterRemoval.class.getName()));

        model.createResource().as(PolyfillSuggestionRule.class)
            .setLabel("Virtuoso - OpDatasetNames to OpGraph")
            .setComment("Rewrite Graph ?g {} to Graph ?g { ?s ?p ?o }")
            .setCondition(virtuoso)
            .setLevel(10100)
            .setSuggestion(model.createResource().as(PolyfillRewriteJava.class)
                .setJavaClass(TransformOpDatasetNamesToOpGraph.class.getName()));

        model.createResource().as(PolyfillSuggestionRule.class)
            .setLabel("Virtuoso - Fix SubStr")
            .setComment("Transform substr expressions to handle the case where the requested length is greater than the string's remaining length")
            .setCondition(model.createResource().as(PolyfillConditionQuery.class)
                    .setQueryString("SELECT (SUBSTR('test', 1, 10) AS ?c) { }"))
            .setLevel(10100)
            .setSuggestion(model.createResource().as(PolyfillRewriteJava.class)
                .setJavaClass(ExprTransformVirtuosoSubstr.class.getName()));

        // RDFDataMgr.write(System.err, model, RDFFormat.TRIG_PRETTY);
    }
}
