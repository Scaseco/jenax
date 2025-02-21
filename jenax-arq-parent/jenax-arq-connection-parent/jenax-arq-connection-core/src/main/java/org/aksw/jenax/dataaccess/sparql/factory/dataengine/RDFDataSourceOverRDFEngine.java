package org.aksw.jenax.dataaccess.sparql.factory.dataengine;

import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.datasource.RDFDataSource;
import org.aksw.jenax.dataaccess.sparql.engine.RDFEngine;
import org.aksw.jenax.dataaccess.sparql.link.transform.LinkSparqlQueryTransformApp;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.transform.RDFLinkTransforms;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.sparql.core.DatasetGraph;

/**
 * A datasource view over an RDFEngine.
 * Note that the engine may be a {@link DecoratedRDFEngine}.
 */
//public class RDFDataSourceOverRDFEngine
//    implements RDFDataSource
//{
//    protected RDFEngine engine;
//
//    protected RDFDataSourceOverRDFEngine(RDFEngine engine) {
//        super();
//        this.engine = Objects.requireNonNull(engine);
//    }
//
//    public static RDFDataSourceOverRDFEngine of(RDFEngine engine) {
//        return new RDFDataSourceOverRDFEngine(engine);
//    }
//
//    @Override
//    public RDFConnection getConnection() {
//        RDFLink link = engine.newLinkBuilder().build();
//
//        DatasetGraph dataset = engine.getDataset();
//        if (dataset != null) {
//            RDFLinkTransform transform = RDFLinkTransforms.of(new LinkSparqlQueryTransformApp(dataset));
//            link = transform.apply(link);
//        }
//
//        // TODO We need to properly handle Dataset-backed engines.
//        //   This means we have to deal with QueryExecutionCompat, QueryExecApp
//        //   so that Resource views work.
//        RDFConnection result = RDFConnectionAdapter.adapt(link);
//        return result;
//    }
//}
