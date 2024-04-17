package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.sparql_path.core.algorithm.ConceptPathFinderSystemBasic;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.path.SimplePath;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.util.PrefixMapping2;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestConceptPathFinder {

    private static final Logger logger = LoggerFactory.getLogger(TestConceptPathFinder.class);

    @Test
    public void testConceptPathFinder01() {
        PrefixMapping pm = PrefixMapping.Extended;

        // TODO Simply specification of reference paths such as by adding a Path.parse method
        List<SimplePath> expected = Arrays.asList(
                SimplePath.fromPropertyPath(PathParser.parse("^eg:bc/^eg:ab", pm)));

        ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();
        List<SimplePath> actual = exec(
            system,
            RDFDataMgr.loadDataset("concept-path-finder-test-data.ttl"),
            Concept.parse("?s { ?s eg:cd ?o }", pm),
            Concept.parse("?s { ?s eg:ab ?o }", pm),
            3);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testConceptPathFinder02() {
        PrefixMapping pm = new PrefixMapping2(PrefixMapping.Extended);
        pm.setNsPrefix("geo", "http://www.opengis.net/ont/geosparql#");
        pm.setNsPrefix("fp7", "http://fp7-pp.publicdata.eu/ontology/");
        pm.setNsPrefix("wgs", "http://www.w3.org/2003/01/geo/wgs84_pos#");

        List<SimplePath> expected = Arrays.asList(
                SimplePath.fromPropertyPath(PathParser.parse("fp7:funding/fp7:partner/fp7:address/fp7:city/owl:sameAs", pm)));

        ConceptPathFinderSystem system = new ConceptPathFinderSystemBasic();

        List<SimplePath> actual = exec(
            system,
            RDFDataMgr.loadDataset("fp7-sample-lod2.ttl"),
            Concept.parse("?s { ?s a fp7:Project }", pm),
            Concept.parse("?x { ?x wgs:lat ?o }", pm),
            3);

        Assert.assertEquals(expected, actual);

//        for (SimplePath path : actual) {
//            System.out.println(path);
//        }
    }

    public static List<SimplePath> exec(
        ConceptPathFinderSystem system,
        Dataset ds,
        Fragment1 source,
        Fragment1 target,
        int n) {
        // Load some test data and create a sparql connection to it
        RdfDataSource dataSource = () -> RDFConnection.connect(ds);

        //dataConnection.update("DELETE WHERE { ?s a ?t }");

        // Use the system to compute a data summary
        // Note, that the summary could be loaded from any place, such as a file used for caching
        Model dataSummary = system.computeDataSummary(dataSource).blockingGet();

        // RDFDataMgr.write(System.out, dataSummary, RDFFormat.TURTLE_PRETTY);

        // Build a path finder; for this, first obtain a factory from the system
        // set its attributes and eventually build the path finder.
        ConceptPathFinder pathFinder = system.newPathFinderBuilder()
            .setDataSummary(dataSummary)
            .setDataSource(dataSource)
            .setShortestPathsOnly(false)
            .build();


        // Concept.parse("?s | ?s ?p [ a eg:D ]", PrefixMapping.Extended),
        // Create search for paths between two given sparql concepts
        PathSearch<SimplePath> pathSearch = pathFinder.createSearch(
            source,
            target);
            //Concept.parse("?s | ?s a eg:A", PrefixMapping.Extended));

        // Set parameters on the search, such as max path length and the max number of results
        // Invocation of .exec() executes the search and yields the flow of results
        List<SimplePath> actual = pathSearch
                .setMaxPathLength(n)
                //.setMaxResults(100)
                .exec()
                .toList().blockingGet();

//		System.out.println("Paths");
//		actual.forEach(System.out::println);
        return actual;
    }
}
