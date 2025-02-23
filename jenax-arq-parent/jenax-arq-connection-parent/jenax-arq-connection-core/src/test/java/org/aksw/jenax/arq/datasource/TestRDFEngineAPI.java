package org.aksw.jenax.arq.datasource;

import java.util.Map;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngines;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineDecorator;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactory;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryLegacyBase;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RDFEngineFactoryRegistry;
import org.aksw.jenax.dataaccess.sparql.factory.dataengine.RdfDataEngineFromDataset;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransforms;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.junit.Assert;
import org.junit.Test;


class MyRdfDataEngineFactoryMem
    extends RDFEngineFactoryLegacyBase
{
    @Override
    public RDFEngine create(Map<String, Object> config) {
        DatasetGraph dsg = DatasetGraphFactory.create();
        RDFEngine result = RdfDataEngineFromDataset.create(dsg);
        return result;
    }
}


public class TestRDFEngineAPI {
    @Test
    public void test() throws Exception {
        RDFEngineFactoryRegistry registry = new RDFEngineFactoryRegistry();

        // Register our own engine factory
        registry.putFactory("test-mem", new MyRdfDataEngineFactoryMem());

        // Get the builder for a specific DBMS (e.g. tdb2, virtuoso, qlever, ...)
        // In this case we just use Jena's default in-memory engine
        RDFEngineFactory engineFactory = registry.getEngineFactory("test-mem");

        // Build a concrete instance of the engine and start it.
        RDFEngine engine = engineFactory.newEngineBuilder()
            // Delete all generated database files upon closing the engine
            // This is a no-op for in-memory engines
            // In is the responsibility of the engine's driver to implement this feature.
            .setAutoDeleteIfCreated(true)
            .build();

        // Get the engine's link source to run some updates.
        // Link sources should not require resource management.
        // If a link source does need it then resource management should
        // be woven into an accompanying RDFEngine.
        try (RDFLink link = engine.getLinkSource().newLink()) {
            link.update("INSERT DATA { <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .}");
            link.update("INSERT DATA { <urn:s> <urn:p> <urn:o> .}");
        }

        // Decorate the raw engine. The decorated RDF engine inherits the resource
        // management of the base engine.
        RDFEngineDecorator<?> engineDecorator = RDFEngines.decorate(engine);
        engineDecorator.decorate(RDFLinkTransforms.withLimit(1));

        try (RDFEngine decoratedEngine = engineDecorator.build()) {
            RDFDataSource dataSource = decoratedEngine.getLinkSource().asDataSource();

            // Test 1: The Resource abstraction is expected to work.
            TestResource.testResource(dataSource);

            // Test 2: The "limit 1" decorator is expected to work.
            Table table = dataSource.asLinkSource().newQuery().query("SELECT * { ?s ?p ?o }").table();
            Assert.assertEquals(table.size(), 1);
        }
    }
}
