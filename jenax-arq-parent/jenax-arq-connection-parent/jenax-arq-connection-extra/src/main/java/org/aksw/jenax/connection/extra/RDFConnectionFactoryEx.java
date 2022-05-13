package org.aksw.jenax.connection.extra;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jenax.arq.connection.RDFConnectionModular;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactoryOverSparqlQueryConnection;
import org.aksw.jenax.arq.connection.core.SparqlQueryConnectionJsa;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;

public class RDFConnectionFactoryEx {

    // TODO Consider move to a better place - e.g. RDFConnectionFactoryEx


    public static RDFConnectionEx connect(SparqlServiceReference ssr) {
        return connect(ssr.getServiceURL(), ssr.getDatasetDescription());
    }

    public static RDFConnectionEx connect(String serviceUrl, DatasetDescription datasetDescription) {
        RDFConnection rawConn = RDFConnectionFactory.connect(serviceUrl);
        RDFConnection core = wrapWithDatasetAndXmlContentType(rawConn, datasetDescription);

        RDFConnectionMetaData metadata = ModelFactory.createDefaultModel()
                .createResource().as(RDFConnectionMetaData.class);

        metadata.setServiceURL(serviceUrl);
        metadata.getDefaultGraphs().addAll(datasetDescription.getDefaultGraphURIs());
        metadata.getNamedGraphs().addAll(datasetDescription.getNamedGraphURIs());

        RDFConnectionEx result = new RDFConnectionExImpl(core, metadata);

        return result;
    }

    /**
     * Wrap a connection with one that provides metadata.
     * If the given metadata is null, an empty blank node will be created.
     *
     * @param rawConn
     * @param metadata
     * @return
     */
    public static RDFConnectionEx wrap(RDFConnection rawConn, Resource metadata) {
        if(metadata == null) {
            metadata = ModelFactory.createDefaultModel().createResource();
        }

        RDFConnectionMetaData md = metadata.as(RDFConnectionMetaData.class);

        RDFConnectionEx result = new RDFConnectionExImpl(rawConn, md);
        return result;
    }


    public static RDFConnection wrapWithDatasetAndXmlContentType(RDFConnection rawConn, DatasetDescription datasetDescription) {
//        RDFConnection result =
//                new RDFConnectionModular(new SparqlQueryConnectionJsa(
//                        FluentQueryExecutionFactory
//                            .from(new QueryExecutionFactorySparqlQueryConnection(rawConn))
//                            .config()
//                                //.withClientSideConstruct()
//                                .withDatasetDescription(datasetDescription)
//                                .withPostProcessor(qe -> {
//                                    if(qe instanceof QueryExecutionHTTP) {
//                                        QueryExecutionHTTP qeh = (QueryExecutionHTTP)qe;
//                                        qeh.setSelectContentType(WebContent.contentTypeResultsXML);
//                                        qeh.setModelContentType(WebContent.contentTypeNTriples);
//                                        qeh.setDatasetContentType(WebContent.contentTypeNQuads);
//                                    }
//                                })
//                                .end()
//                            .create()
//                            ), rawConn, rawConn);
//
//
//        return result;
        throw new UnsupportedOperationException();
    }

}
