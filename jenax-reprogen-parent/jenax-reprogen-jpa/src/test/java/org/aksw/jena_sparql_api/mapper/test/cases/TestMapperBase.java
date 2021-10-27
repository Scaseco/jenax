package org.aksw.jena_sparql_api.mapper.test.cases;

import javax.persistence.EntityManager;

import org.aksw.jena_sparql_api.mapper.impl.engine.RdfMapperEngineImpl;
import org.aksw.jena_sparql_api.mapper.jpa.core.EntityManagerImpl;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jenax.arq.util.dataset.DatasetDescriptionUtils;
import org.aksw.jenax.arq.util.syntax.ElementTransformDatasetDescription;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.connectionless.SparqlService;
import org.aksw.jenax.stmt.parser.query.SparqlQueryParserImpl;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.junit.Before;

public class TestMapperBase {
    protected String graphName;
    protected Dataset ds;
    protected SparqlService sparqlService;
    protected RdfMapperEngineImpl mapperEngine;
    protected Prologue prologue;
    protected EntityManager entityManager;

    @Before
    public void beforeTest() {
        //String graphName = "http://ex.org/graph/";
        graphName = "http://ex.org/graph/";
        ds = DatasetFactory.createGeneral();
        DatasetDescription dd = DatasetDescriptionUtils.createDefaultGraph(graphName);
        sparqlService = FluentSparqlService.from(ds)
                .config()
                    .configQuery()
                        .withParser(SparqlQueryParserImpl.create())
                    .end()
                    .withDatasetDescription(dd, graphName)
                    .configQuery()
                        .withQueryTransform(ElementTransformDatasetDescription::rewrite)
                        .withQueryTransform(query -> QueryUtils.applySlice(query, null, 1000l, true)) //F_QueryTransformLimit.create(1000))
                    .end()
                .end()
                .create();

        prologue = new Prologue();
        prologue.setPrefix("o", "http://example.org/ontololgy/");
        prologue.setPrefix("foaf", FOAF.NS);

        mapperEngine = new RdfMapperEngineImpl(sparqlService, prologue);


        entityManager = new EntityManagerImpl(mapperEngine);
    }
}
