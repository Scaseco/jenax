package org.aksw.jena_sparql_api.conjure.datapod.impl;

import java.io.IOException;
import java.net.Authenticator;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.commons.io.util.UriUtils;
import org.aksw.commons.util.ref.Ref;
import org.aksw.commons.util.ref.RefImpl;
import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRef;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefSparqlEndpoint;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefUrl;
import org.aksw.jena_sparql_api.conjure.dataref.core.api.DataRefVisitor;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfAuthBasic;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfAuthBearerToken;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.RdfDataRef;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.Op;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpDataRefResource;
import org.aksw.jena_sparql_api.conjure.dataset.algebra.OpVisitor;
import org.aksw.jena_sparql_api.conjure.dataset.engine.ExecutionUtils;
import org.aksw.jena_sparql_api.http.repository.api.HttpResourceRepositoryFromFileSystem;
import org.aksw.jena_sparql_api.http.repository.api.RdfHttpEntityFile;
import org.aksw.jena_sparql_api.http.repository.impl.HttpResourceRepositoryFromFileSystemImpl;
import org.aksw.jena_sparql_api.io.hdt.JenaPluginHdt;
import org.aksw.jenax.arq.connection.core.RDFConnectionUtils;
import org.aksw.jenax.arq.connection.dataset.DatasetRDFConnectionFactoryBuilder;
import org.aksw.jenax.connection.datasource.RdfDataSource;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.aksw.jenax.sparql.query.rx.RDFDataMgrEx;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.WebContent;
import org.apache.jena.util.ResourceUtils;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic DataPod factory methods
 *
 * @author raven
 *
 */
public class DataPods {
    private static final Logger logger = LoggerFactory.getLogger(DataPods.class);

    public static RdfDataPod fromDataRef(RdfDataRef dataRef) {
        //OpExecutorDefault catalogExecutor = new OpExecutorDefault(null, null, new LinkedHashMap<>(), RDFFormat.TURTLE_PRETTY);

        Resource rawCopy = dataRef.inModel(ResourceUtils.reachableClosure(dataRef));
        RdfDataRef copy = JenaPluginUtils.polymorphicCast(rawCopy, RdfDataRef.class);
        Op basicWorkflow = OpDataRefResource.from(copy.getModel(), copy);
        RdfDataPod result = ExecutionUtils.executeJob(basicWorkflow);
        //RdfDataPod result = basicWorkflow.accept(catalogExecutor);
        return result;
    }

    public static RdfDataPod empty() {
        RdfDataPod result = fromModel(ModelFactory.createDefaultModel());
        return result;
    }

    public static RdfDataPod fromData(Object data) {
        if(data != null) {
            throw new RuntimeException("not implemented yet");
        }
        RdfDataPod result = fromModel(ModelFactory.createDefaultModel());
        return result;
    }

    public static RdfDataPod fromDataRef(DataRef dataRef, Dataset dataset, HttpResourceRepositoryFromFileSystem repo, OpVisitor<? extends RdfDataPod> opExecutor) {

        DataRefVisitor<RdfDataPod> factory = new DataPodFactoryAdvancedImpl(dataset, opExecutor, repo);

//		System.out.println("Got: " + dataRef + " - class: " + dataRef.getClass() + " inst:" + (dataRef instanceof DataRefResourceFromUrl));
        RdfDataPod result = dataRef.accept(factory);
        return result;
    }


    public static RdfDataPod fromDataRef(DataRef dataRef, OpVisitor<? extends RdfDataPod> opExecutor) {
        DataRefVisitor<RdfDataPod> defaultFactory = new DataPodFactoryImpl(opExecutor);

//		System.out.println("Got: " + dataRef + " - class: " + dataRef.getClass() + " inst:" + (dataRef instanceof DataRefResourceFromUrl));
        RdfDataPod result = dataRef.accept(defaultFactory);
        return result;
    }

    public static RdfDataPod fromDataset(Dataset dataset) {
        return new RdfDataPodBase() {
            @Override
            protected RDFConnection newConnection() {
                // The simple connection approach is broken with jena 4.3.x because
                // QueryExecutionCompat.get() returns a null QueryExec
                // RDFConnection result = RDFConnection.connect(dataset);
                RDFConnection result = DatasetRDFConnectionFactoryBuilder.connect(dataset);
                return result;
            }

            /**
             * Only returns the default model
             */
            @Override
            public Model getModel() {
                Model r = dataset.getDefaultModel();
                return r;
            }
        };
    }


    public static RdfDataPod fromModel(Model model) {
        Dataset dataset = DatasetFactory.wrap(model);

        RdfDataPod result = fromDataset(dataset);
        return result;

//		return new RdfDataPodBase() {
//			@Override
//			protected RDFConnection newConnection() {
//				RDFConnection result = RDFConnectionFactory.connect(dataset);
//				return result;
//			}
//
//			@Override
//			public Model getModel() {
//				return model;
//			}
//		};
    }

    public static RdfDataPod fromUrl(DataRefUrl dataRef) {
        String url = dataRef.getDataRefUrl();
        RdfDataPod result = fromUrl(url);
        return result;
    }


    public static RdfDataPod fromUrl(String url) {
        logger.info("Loading: " + url);

        RdfDataPod result;
        Lang lang = RDFLanguages.resourceNameToLang(url);
        if(JenaPluginHdt.LANG_HDT.equals(lang)) {
            logger.info("HDT file detected - loading using HDT graph " + url);
            // Only allow local file URLs
            Path path = Paths.get(UriUtils.newURI(url));
            String pathStr = path.toString();

            HDT hdt;
            try {
                //hdt = HDTManager.loadHDT(pathStr);
                // Map seems to be significantly faster than load for cases where we have
                // to scan all triples anyway
                // TODO The load method should take an example query load in order to decide
                // the best way of loading
                hdt = HDTManager.mapHDT(pathStr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.info("Loading of hdt complete " + pathStr);

            Ref<HDT> hdtRef = RefImpl.create(hdt, null,
                    () -> {
                        logger.debug("Closed HDT file: " + pathStr);
                        hdt.close();
                    }, "HDT Data Pod from " + pathStr);
            result = new RdfDataPodHdtImpl(hdtRef, false);
        } else {
            // Model model;
            // model = RDFDataMgr.loadModel(url);
            Dataset dataset = RDFDataMgrEx.loadDatasetAsGiven(url, null);
            result = DataPods.fromDataset(dataset); // fromModel(model);
        }


        return result;
    }


    public static RdfDataPod create(String url, HttpResourceRepositoryFromFileSystem repo) {
        RdfDataPod r;

        // HACK - http url checking should be done in the repository!
        if(url.startsWith("http://") || url.startsWith("https://")) {
            RdfHttpEntityFile entity;
            try {
                HttpUriRequest baseRequest =
                        RequestBuilder.get(url)
                        .setHeader(HttpHeaders.ACCEPT, "application/x-hdt")
                        .setHeader(HttpHeaders.ACCEPT_ENCODING, "identity,bzip2,gzip")
                        .build();

                HttpRequest effectiveRequest = HttpResourceRepositoryFromFileSystemImpl.expandHttpRequest(baseRequest);
                logger.info("Expanded HTTP Request: " + effectiveRequest);

                entity = repo.get(effectiveRequest, HttpResourceRepositoryFromFileSystemImpl::resolveRequest);

                logger.info("Response entity is: " + entity);

//				repo.get(, HttpResourceRepositoryFromFileSystemImpl::resolveRequest);

//				entity = HttpResourceRepositoryFromFileSystemImpl.get(repo,
//						url, WebContent.contentTypeNTriples, Arrays.asList("identity"));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Path absPath = entity.getAbsolutePath();
            logger.info("Resolved " + url + " to " + absPath);

            r = DataPods.fromUrl(absPath.toUri().toString());
        } else {
            r = DataPods.fromUrl(url);
        }

        return r;
    }
    public static RdfDataPod fromSparqlEndpoint(DataRefSparqlEndpoint dataRef) {
        String serviceUrl = dataRef.getServiceUrl();
        //DatasetDescription dd = dataRef.getDatsetDescription();

        List<String> defaultGraphs = dataRef.getDefaultGraphs();
        List<String> namedGraphs = dataRef.getNamedGraphs();

        Object auth = dataRef.getAuth();

        RdfDataPod result = fromSparqlEndpoint(serviceUrl, defaultGraphs, namedGraphs, auth);
        return result;
    }

    public static RdfDataPod fromSparqlEndpoint(String serviceUrl, List<String> defaultGraphs, List<String> namedGraphs, Object auth) {

        Objects.requireNonNull(serviceUrl, "Service URL must not be null");

        // Make immutable copies of the arguments
        List<String> defaultGraphsCpy = defaultGraphs == null ? null : new ArrayList<>(defaultGraphs);
        List<String> namedGraphsCpy = namedGraphs == null ? null : new ArrayList<>(namedGraphs);

        Authenticator authenticator = null;
        if (auth instanceof RdfAuthBasic) {
            RdfAuthBasic authData = (RdfAuthBasic)auth;
            String username = authData.getUsername();
            String password = authData.getPassword();
            authenticator = AuthLib.authenticator(username, password);
        }

        if (auth instanceof RdfAuthBearerToken) {
            RdfAuthBearerToken authData = (RdfAuthBearerToken)auth;
            String bearerToken = authData.getBearerToken();
            // tmpHttpRequestModifier = builder -> builder.setHeader(HttpNames.hAuthorization, "Bearer " + bearerToken);
            // FIXME BIG SECURITY ISSUE - users on the same endpoint will override their bearer token - so they
            // might see data they shouldn't have access to
            // Needs a PR to Jena - e.g. RDFConnectionBuilderHttp might need a way to inject http request mods
            AuthEnv.get().setBearerToken(serviceUrl, bearerToken);
        }

        Builder httpClientBuilder = HttpClient.newBuilder();
        if (authenticator != null) {
            httpClientBuilder = httpClientBuilder.authenticator(authenticator);
        }

        HttpClient httpClient = httpClientBuilder.build();


        RdfDataPod result = new RdfDataPod() {

            @Override
            public RDFConnection getConnection() {

                RDFConnectionRemoteBuilder rdfConnectionBuilder = RDFConnectionRemote.create()
                        .destination(serviceUrl)
                        .acceptHeaderSelectQuery(WebContent.contentTypeResultsXML) // JSON breaks on virtuoso with empty result sets
                        ;

                // FIXME We cannot set default / named graphs on the remote builder

                rdfConnectionBuilder = rdfConnectionBuilder.httpClient(httpClient);

                RDFConnection r = rdfConnectionBuilder.build();

                r = RDFConnectionUtils.wrapWithQueryTransform(r,
                    query -> {
                        boolean updateDefaultGraphs = query.getGraphURIs().isEmpty() && defaultGraphsCpy != null && !defaultGraphsCpy.isEmpty();
                        boolean updateNamedGraphs = query.getNamedGraphURIs().isEmpty() && namedGraphsCpy != null && !namedGraphsCpy.isEmpty();
                        Query res;
                        if (updateDefaultGraphs || updateNamedGraphs) {
                            res = query.cloneQuery();
                            if (updateDefaultGraphs) {
                                res.getGraphURIs().addAll(defaultGraphsCpy);
                            }
                            if (updateNamedGraphs) {
                                res.getNamedGraphURIs().addAll(namedGraphsCpy);
                            }
                        } else {
                            res = query;
                        }
                        return res;
                    });

                return r;
            }

            @Override
            public void close() throws Exception {
                // Java 21 - https://stackoverflow.com/questions/53919721/close-java-http-client
                // httpClient.close();
            }

            @Override
            public boolean isMutable() {
                return true;
            }
        };

        // RdfDataPod result = fromDataSource(dataSource);
        return result;
    }

    public static RdfDataPod fromDataSource(RdfDataSource supplier) {
        return new RdfDataPodBase() {
            @Override
            protected RDFConnection newConnection() {
                RDFConnection result = supplier.getConnection();
                return result;
            }
        };
    }
}
