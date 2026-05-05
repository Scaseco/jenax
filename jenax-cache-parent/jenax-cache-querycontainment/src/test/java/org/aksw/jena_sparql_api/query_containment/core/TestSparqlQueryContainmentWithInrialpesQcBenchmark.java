package org.aksw.jena_sparql_api.query_containment.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcReader;
import org.aksw.jena_sparql_api.resources.sparqlqc.SparqlQcVocab;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//@FixMethodOrder
//@Disabled
public class TestSparqlQueryContainmentWithInrialpesQcBenchmark {

    private static final Logger logger = LoggerFactory.getLogger(TestSparqlQueryContainmentWithInrialpesQcBenchmark.class);


    static Stream<Object[]> data() throws Exception {
        List<Object[]> params = new ArrayList<>();
        params.addAll(createTestParams("sparqlqc/1.4/benchmark/cqnoproj.rdf"));
        params.addAll(createTestParams("sparqlqc/1.4/benchmark/ucqproj.rdf"));
        return params.stream();
    }


    public static List<Object[]> createTestParams(String testCases) throws IOException {
        List<Resource> ts = SparqlQcReader.loadTasks(testCases);

        List<Object[]> result = new ArrayList<>();
        for(int i = 0; i < ts.size(); ++i) {
            Resource t = ts.get(i);
            Object[] data = new Object[3];
            data[0] = t.getURI(); //testCase.getName();
            data[1] = t.getModel();
            data[2] = t;
        

            boolean hackToTestOnlyASingleTask = false;
            if(hackToTestOnlyASingleTask) {
                if(!t.getURI().equals("http://sparql-qc-bench.inrialpes.fr/CQNoProj#nop16")) {
                    continue;
                }
            }

            result.add(data);
        }

        return result;
    }

    @ParameterizedTest
    @MethodSource("data")
    void runTest(String name, Model model, Resource t) {

   // Main method for profiling the test cases with visualvm ; the junit stuff is distracting...
    public static void main(String[] args) throws Exception {
        List<Object[]> data = data().collect(java.util.stream.Collectors.toList());
        for(Object[] o : data) {
            runTest((String)o[0], (Model)o[1], (Resource)o[2]);
        }
    }

          String srcQueryStr = t.getRequiredProperty(SparqlQcVocab.sourceQuery).getObject().asResource().getRequiredProperty(SparqlQcVocab.sparqlQueryString).getObject().asLiteral().getString();
        String tgtQueryStr = t.getRequiredProperty(SparqlQcVocab.targetQuery).getObject().asResource().getRequiredProperty(SparqlQcVocab.sparqlQueryString).getObject().asLiteral().getString();
        boolean expectedVerdict = Boolean.parseBoolean(t.getRequiredProperty(SparqlQcVocab.result).getObject().asLiteral().getString());

        Set<String> overrides = new HashSet<>(Arrays.asList(
            "http://sparql-qc-bench.inrialpes.fr/UCQProj#p24", // This is not the type of query we want to use for caching (the view is a union which partially matches into the user query)
            // TODO Fix the test case below:
            "http://sparql-qc-bench.inrialpes.fr/UCQProj#p26", // CARE! A view must not have more quad patterns than the query ; so the benchmark is correct - This consideration was WRONG: I think this is a bug in the benchmark; the expected result is wrong
            "http://sparql-qc-bench.inrialpes.fr/UCQProj#p27"  // Like p24; we require exact match of all of the views union members
        ));

        boolean overridden = overrides.contains(t.getURI());
        if(overridden) {
            expectedVerdict = !expectedVerdict;
        }


        Query viewQuery = SparqlQueryContainmentUtils.queryParser.apply(tgtQueryStr);
        Query userQuery = SparqlQueryContainmentUtils.queryParser.apply(srcQueryStr);

        logger.debug("Test case: " + t);
        logger.debug("View Query: " + viewQuery);
        logger.debug("User Query: " + userQuery);
        
        // Index validation: If true, optimized subgraph isomorphism (si) lookup results are
        // additionally compared to the results of a non-optimized si-index
        // Should be true for 'production' test cases
        boolean useIndexValidation = true;
        
        boolean actualVerdict = SparqlQueryContainmentUtils.tryMatch(viewQuery, userQuery, useIndexValidation);

        logger.debug("Expected: " + expectedVerdict + " " + (overridden ? "(overridden)" : "") + " - Actual: " + actualVerdict + " Mismatch: " + (expectedVerdict != actualVerdict));

        Assertions.assertEquals(expectedVerdict, actualVerdict);
    }
}


// Stuff below can probably be just removed
//public Query resolve(Model model, String id) {
//	Resource r = model.getResource(id);
//	System.out.println("YAY: " + id);
//	r.getModel().write(System.out, "TURTLE");
//
//	String str = r.getRequiredProperty(LSQ.text)
//			.getObject().asLiteral().getString();
//	Query result = SparqlQueryContainmentUtils.queryParser.apply(str);
//	return result;
//}

//public Query resolve(Model model, String id) {
//	Matcher m = SparqlQcReader.queryNamePattern.matcher(id);
//	m.find();
//	String uri = "http://ex.org/query/" + m.group("id") + "-" + m.group("variant");
//	Query result = extractQuery(model,  uri);
//	return result;
//}
//
//public Query extractQuery(Model model, String uri) {
//	Resource r = model.getResource(uri);
//
//	String str = r.getRequiredProperty(LSQ.text)
//			.getObject().asLiteral().getString();
//	Query result = SparqlQueryContainmentUtils.queryParser.apply(str);
//	return result;
//}

//
//@Test
//public void testProjection() throws IOException {
//	Model tests = ModelFactory.createDefaultModel();
//	RDFDataMgr.read(tests, new ClassPathResource("sparqlqc/1.4/benchmark/ucqproj.rdf").getInputStream(), Lang.RDFXML);
//
//  Model model = SparqlQcReader.readResources("sparqlqc/1.4/benchmark/projection/*");
//  List<Resource> ts = tests.listResourcesWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toList();
//
//  for(Resource t : ts) {
//  	runTest(model, t);
//  }
//
//}
//
//@Test
//public void test() throws IOException {
//	/*
//	tryMatch(
//		String.join("\n",
//			"?x <my://type> <my://Airport> .",
//  		"?x <my://label> ?n ; ?h ?i . ",
//  		"FILTER(langMatches(lang(?n), 'en')) .",
//  		"FILTER(<mp://fn>(?x, ?n))"),
//
//		String.join("\n",
//      		"?s <my://type> <my://Airport> .",
//      		"?s ?p ?l .",
//      		"FILTER(?p = <my://label> || ?p = <my://name>)")
//	);
//*/
//	Model tests = ModelFactory.createDefaultModel();
//	RDFDataMgr.read(tests, new ClassPathResource("sparqlqc/1.4/benchmark/cqnoproj.rdf").getInputStream(), Lang.RDFXML);
//
//  Model model = SparqlQcReader.readResources("sparqlqc/1.4/benchmark/noprojection/*");
//  //model.write(System.out, "TURTLE");
//
//  List<Resource> ts = tests.listResourcesWithProperty(RDF.type, SparqlQcVocab.ContainmentTest).toList();
//
//  Stopwatch sw = Stopwatch.createStarted();
//  int j = 0;
//  for(int i = 0; i < 1; ++i) {
//      for(Resource t : ts) {
//      	++j;
//      	runTest(model, t);
//      }
//  }
//  System.out.println("Avg: " + sw.elapsed(TimeUnit.MILLISECONDS) / (double)j + " - " + j);
//}

//public void runTest(Model model, Resource t) {
