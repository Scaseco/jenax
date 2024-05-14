package org.aksw.jena_sparql_api.sparql_path.impl.bidirectional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinder;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderBase;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderFactorySummaryBase;
import org.aksw.jena_sparql_api.sparql_path.api.ConceptPathFinderSystem;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearch;
import org.aksw.jena_sparql_api.sparql_path.api.PathSearchSparqlBase;
import org.aksw.jena_sparql_api.sparql_path.core.PathConstraint3;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.dataaccess.sparql.factory.datasource.RdfDataSources;
import org.aksw.jenax.sparql.fragment.api.Fragment1;
import org.aksw.jenax.sparql.fragment.impl.Concept;
import org.aksw.jenax.sparql.fragment.impl.ConceptUtils;
import org.aksw.jenax.sparql.path.SimplePath;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtParserImpl;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.aksw.jenax.stmt.util.SparqlStmtUtils;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.DatasetDescription;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class ConceptPathFinderSystem3
    implements ConceptPathFinderSystem
{
    @Override
    public Single<Model> computeDataSummary(RdfDataSource dataSource) {
        InputStream in = ConceptPathFinderBidirectionalUtils.class.getClassLoader().getResourceAsStream("concept-path-finder-type-local.sparql");
        //Stream<SparqlStmt> stmts;
        Flowable<SparqlStmt> stmts;
        stmts = Flowable.fromIterable(() -> {
            try {
                return SparqlStmtUtils.parse(in, SparqlStmtParserImpl.create(Syntax.syntaxARQ, true));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Single<Model> result = stmts
            //.peek(System.out::println)
            .filter(SparqlStmt::isQuery)
            .map(SparqlStmt::getAsQueryStmt)
            .map(SparqlStmtQuery::getQuery)
            .filter(q -> q.isConstructType())
            .map(q -> RdfDataSources.exec(dataSource, q, QueryExecution::execConstruct))
            .toList()
            .map(list -> {
                Model r = ModelFactory.createDefaultModel();
                list.forEach(r::add);
                return r;
            });

        return result;
    }

    @Override
    public ConceptPathFinderFactoryBidirectional<?> newPathFinderBuilder() {
        return new ConceptPathFinderFactoryBidirectional<>();
    }

    public static class ConceptPathFinderFactoryBidirectional<T extends ConceptPathFinderFactoryBidirectional<T>>
        extends ConceptPathFinderFactorySummaryBase<T>
    {
        // NOTE We could add more specific attributes here if we wanted
        @Override
        public ConceptPathFinder build() {
            return new ConceptPathFinderBase(dataSummary.getGraph(), dataSource) {
                @Override
                public PathSearch<SimplePath> createSearch(Fragment1 sourceConcept, Fragment1 targetConcept) {

                    return new PathSearchSparqlBase(dataSource, sourceConcept, targetConcept) {

                        @Override
                        public Flowable<SimplePath> execCore() {
//							pathValidators,
//							ConceptPathFinderBidirectionalUtils::convertGraphPathToSparqlPath3
                            Flowable<SimplePath> result = ConceptPathFinderBidirectionalUtils
                                .findPathsCore(dataSource,
                                        sourceConcept,
                                        targetConcept,
                                        maxResults,
                                        maxLength,
                                        dataSummary,
                                        shortestPathsOnly,
                                        simplePathsOnly,
                                        pathValidators,
                                        new PathConstraint3(),
                                        ConceptPathFinderBidirectionalUtils::convertGraphPathToSparqlPath3);

                            return result;
                        }
                    };
                }
            };
        }
    }

    public static void main(String[] args) throws Exception {
        Model m = RDFDataMgr.loadModel("/home/raven/Projects/Eclipse/faceted-browsing-benchmark-parent/faceted-browsing-benchmark-parent/faceted-browsing-benchmark-v2-parent/faceted-browsing-benchmark-v2-core/src/main/resources/path-data-simple.ttl");

        RdfDataSource dataSource = () -> RDFConnection.connect(DatasetFactory.wrap(m));

        ConceptPathFinderSystem system = new ConceptPathFinderSystem3();
        Model dataSummary = system.computeDataSummary(dataSource).blockingGet();

        ConceptPathFinder pathFinder = system.newPathFinderBuilder()
            .setDataSource(dataSource)
            .setDataSummary(dataSummary)
            .build();


        List<SimplePath> paths = pathFinder.createSearch(ConceptUtils.createSubjectConcept(), ConceptUtils.createSubjectConcept())
            .setMaxLength(1l)
            .exec()
            .toList()
            .blockingGet();

        System.out.println("Paths: " + paths);
    }


    public static void main3(String[] args) throws Exception {
        DatasetDescription datasetDescription = new DatasetDescription();
        //datasetDescription.addDefaultGraphURI("http://dbpedia.org/wkd_uris");
        datasetDescription.addDefaultGraphURI("http://dbpedia.org");

        RdfDataSource dataSource = wrapWithDatasetAndXmlContentType("http://localhost:8890/sparql", datasetDescription);
        ConceptPathFinderSystem system = new ConceptPathFinderSystem3();
        Model model = system.computeDataSummary(dataSource).blockingGet();

        RDFDataMgr.write(new FileOutputStream("/home/raven/dbpedia-data-summary.ttl"), model, RDFFormat.TURTLE_PRETTY);
    }

    public static RdfDataSource wrapWithDatasetAndXmlContentType(String url, DatasetDescription datasetDescription) {
//        qeh.setSelectContentType(WebContent.contentTypeResultsXML);
//        qeh.setModelContentType(WebContent.contentTypeNTriples);
//        qeh.setDatasetContentType(WebContent.contentTypeNQuads);

        RdfDataSource result = () -> RDFConnectionRemote.newBuilder()
            .destination(url)
            .acceptHeaderSelectQuery(WebContent.contentTypeResultsXML)
            .acceptHeaderGraph(WebContent.contentTypeNTriples)
            .acceptHeaderDataset(WebContent.contentTypeNQuads)
            .build();

        return result;
    }


    public static void main2(String[] args) {
        DatasetDescription datasetDescription = new DatasetDescription();
        //datasetDescription.addDefaultGraphURI("http://dbpedia.org/wkd_uris");
        datasetDescription.addDefaultGraphURI("http://project-hobbit.eu/benchmark/fbb2/");

        RdfDataSource dataSource = wrapWithDatasetAndXmlContentType("http://localhost:8890/sparql", datasetDescription);


        //RDFConnection baseConn = RDFConnectionFactory.connect(DatasetFactory.create());

        // Wrap the connection to use a different content type for queries...
        // Jena rejects some of Virtuoso's json output

//			ConceptPathFinderSystem system = new ConceptPathFinderSystemBidirectional();
        ConceptPathFinderSystem system = new ConceptPathFinderSystem3();
        Model model = system.computeDataSummary(dataSource).blockingGet();

        RDFDataMgr.write(System.out, model, RDFFormat.TURTLE_PRETTY);


        ConceptPathFinder conceptPathFinder = system.newPathFinderBuilder()
            .setDataSource(dataSource)
            .setDataSummary(model)
            .build();

        PrefixMapping prefixes = PrefixMapping.Extended;

        PathSearch<SimplePath> pathSearch = conceptPathFinder.createSearch(
                Concept.create("?src <http://data.nasa.gov/qudt/owl/qudt#unit> ?o", "src", prefixes),
//					Concept.create("?src <http://www.w3.org/ns/ssn/#hasValue> ?o", "src", prefixes),
                Concept.create("?tgt a <http://www.agtinternational.com/ontologies/lived#CurrentObservation>", "tgt", prefixes));


        List<SimplePath> paths = pathSearch
                .setMaxPathLength(3)
                .exec()
                .timeout(10, TimeUnit.SECONDS)
                .toList().blockingGet();


        System.out.println("Paths:");
        for(SimplePath path : paths) {
            System.out.println(path);
        }

        System.out.println("done.");
    }
}
