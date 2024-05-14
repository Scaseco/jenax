package org.aksw.jenax.arq.util.quad;

import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.resultset.ResultSetCompare;

// TODO Move this class to a better place - e.g. jena-sparql-api-utils
public class DatasetCmp {
    /**
     * Check two datasets for isomorphism using comparison by value.
     * Internally converts the datasets into result sets with ?g ?s ?p ?o) bindings
     * and compares them using {@link ResultSetCompare#equalsByValue(org.apache.jena.query.ResultSet, org.apache.jena.query.ResultSet)}
     *
     * <b>Does not scale to large datasets. For large number of named graphs use
     * {@link DatasetCmp#assessIsIsomorphicByGraph(Dataset, Dataset)}</b>
     *
     * @param expected
     * @param actual
     * @param compareByValue 'false' tests for equivalence of terms whereas 'true' tests for that of values
     * @param out The outputstream to which to display any differences
     * @return
     */
    public static boolean isIsomorphic(
            Dataset expected,
            Dataset actual,
            boolean compareByValue,
            PrintStream out,
            Lang lang) {
        boolean result;

        String everything = "SELECT ?g ?s ?p ?o { { GRAPH ?g { ?s ?p ?o } } UNION { ?s ?p ?o } }";
//		String everything = "SELECT ?o { { { GRAPH ?g { ?s ?p ?o } } UNION { ?s ?p ?o } } FILTER(isNumeric(?o))}";
        try (QueryExecution qea = QueryExecutionFactory.create(everything, expected);
             QueryExecution qeb = QueryExecutionFactory.create(everything, actual)) {

            ResultSetRewindable rsa = ResultSetFactory.copyResults(qea.execSelect());
            ResultSetRewindable rsb = ResultSetFactory.copyResults(qeb.execSelect());

            result = compareByValue
                    ? ResultSetCompare.equalsByValue(rsa, rsb)
                    : ResultSetCompare.equalsByTerm(rsa, rsb);

            if (!result) {
                rsa.reset();
                rsb.reset();
                out.println("Expected:");
                ResultSetMgr.write(out, rsa, lang);
                out.println("Actual:");
                ResultSetMgr.write(out, rsb, lang);
            }
        }

        return result;
    }

    public static record Report(
        Set<Node> nonIsomorphicGraphs,
        Set<Node> missingGraphsFirst,
        Set<Node> missingGraphsSecond) {
        public Report() {
            this(new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>());
        }

        public boolean isIsomorphic() {
            return missingGraphsFirst.isEmpty() && missingGraphsSecond.isEmpty() && nonIsomorphicGraphs.isEmpty();
        }

        @Override
        public String toString() {
            return
                "Missing graphs first : " + missingGraphsFirst + "\n" +
                "Missing graphs second: " + missingGraphsSecond + "\n" +
                "Non-isomorphic graphs: " + nonIsomorphicGraphs;
        }
    }

    public static Report assessIsIsomorphicByGraph(Dataset expected, Dataset actual) {
        return assessIsIsomorphicByGraph(expected.asDatasetGraph(), actual.asDatasetGraph());
    }

    public static Report assessIsIsomorphicByGraph(DatasetGraph expected, DatasetGraph actual) {

        Report report = new Report();
        // compare default graphs first
        if (!expected.getDefaultGraph().isIsomorphicWith(actual.getDefaultGraph())) {
            report.nonIsomorphicGraphs.add(Quad.defaultGraphIRI);
        }
        // then compare the named graphs
        Set<Node> allNodes = new LinkedHashSet<>();
        expected.listGraphNodes().forEachRemaining(allNodes::add);
        actual.listGraphNodes().forEachRemaining(allNodes::add);

        for (Node g : allNodes) {

            if (!expected.containsGraph(g)) {
                report.missingGraphsFirst().add(g);
            }

            if (!actual.containsGraph(g)) {
                report.missingGraphsSecond().add(g);
            }

            Graph g1 = expected.getGraph(g);
            Graph g2 = actual.getGraph(g);

            if (g1.size() != g2.size()) {
                report.nonIsomorphicGraphs().add(g);
            }

            // Isomorphism check may fail with stack overflow execution if datasets
            // become too large
            // assert(g1.isIsomorphicWith(g2), s"graph <$g> not isomorph")
        }
        return report;
    }


    /*

    val a = Sets.newHashSet(ds1.asDatasetGraph().find())
    val b = Sets.newHashSet(ds2.asDatasetGraph().find())

    val diff = new SetDiff[Quad](a, b)

    System.err.println("Excessive")
    for(x <- diff.getAdded.asScala) {
      System.err.println("  " + x)
    }

    System.err.println("Missing")
    for(x <- diff.getRemoved.asScala) {
      System.err.println("  " + x)
    }

    System.err.println("Report done")
    */

/*
    System.err.println("Dataset 1");
    RDFDataMgr.write(System.err, ds1, RDFFormat.TRIG_PRETTY);
    System.err.println("Dataset 2");
    RDFDataMgr.write(System.err, ds2, RDFFormat.TRIG_PRETTY);
    System.err.println("Datasets printed");
*/
}
