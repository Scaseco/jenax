package org.aksw.jenax.arq.connection.link;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.jena_sparql_api.transform.QueryExecutionFactoryQueryTransform;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFDatasetConnection;
import org.apache.jena.rdflink.LinkDatasetGraph;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkDataset;
import org.apache.jena.rdflink.RDFLinkModular;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

import com.apicatalog.jsonld.http.link.Link;

public class RDFLinkUtils {

    /** Symbol for placing a connection (TODO supplier?) into an arq context */
    public static final Symbol CONNECTION_SYMBOL = Symbol.create("http://jsa.aksw.org/connection");


    public static LinkSparqlQuery unwrapQueryConnection(LinkSparqlQuery conn) {
        LinkSparqlQuery result;
        if(conn instanceof RDFLinkModular) {
            LinkSparqlQuery tmp = getQueryConnection((RDFLinkModular)conn);
            result = unwrapQueryConnection(tmp);
        } else {
            result = conn;
        }

        return result;
    }

    public static LinkSparqlUpdate unwrapUpdateConnection(LinkSparqlUpdate conn) {
        LinkSparqlUpdate result;
        if(conn instanceof RDFLinkModular) {
            LinkSparqlUpdate tmp = getUpdateConnection((RDFLinkModular)conn);
            result = unwrapUpdateConnection(tmp);
        } else {
            result = conn;
        }

        return result;
    }

    public static LinkDatasetGraph unwrapDatasetConnection(LinkDatasetGraph conn) {
        LinkDatasetGraph result;
        if(conn instanceof RDFLinkModular) {
            LinkDatasetGraph tmp = getDatasetConnection((RDFLinkModular)conn);
            result = unwrapDatasetConnection(tmp);
        } else {
            result = conn;
        }

        return result;
    }


    /** Reflective access to an {@link RDFLinkModular}'s queryConnection. */
    public static LinkSparqlQuery getQueryConnection(RDFLinkModular conn) {
        LinkSparqlQuery result;
        try {
            Field f = RDFLinkModular.class.getDeclaredField("queryConnection");
            f.setAccessible(true);
            result = (LinkSparqlQuery)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /** Reflective access to an {@link RDFLinkModular}'s updateConnection. */
    public static LinkSparqlUpdate getUpdateConnection(RDFLinkModular conn) {
        LinkSparqlUpdate result;
        try {
            Field f = RDFLinkModular.class.getDeclaredField("updateConnection");
            f.setAccessible(true);
            result = (LinkSparqlUpdate)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /** Reflective access to an {@link RDFLinkModular}'s datasetConnection. */
    public static LinkDatasetGraph getDatasetConnection(RDFLinkModular conn) {
        LinkDatasetGraph result;
        try {
            Field f = RDFLinkModular.class.getDeclaredField("datasetConnection");
            f.setAccessible(true);
            result = (LinkDatasetGraph)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /** Reflective access to an {@link RDFLinkModular}'s dataset. */
//    public static Dataset getDataset(RDFLinkDataset conn) {
//        Dataset result;
//        try {
//            Field f = RDFLinkDataset.class.getDeclaredField("dataset");
//            f.setAccessible(true);
//            result = (Dataset)f.get(conn);
//        } catch(Exception e) {
//            throw new RuntimeException(e);
//        }
//        return result;
//    }



    public static RDFLink wrapWithContextMutator(RDFLink rawConn) {
        return wrapWithContextMutator(rawConn, cxt -> {});
    }



    /**
     * Places the connection object as a symbol into to context,
     * so that custom functions - notably E_Benchmark can
     * pose further queries to it.
     *
     * FIXME Connections are usually not intended for concurrent use;
     * we should put a connection supplier into the context instead!
     *
     * @param rawConn
     * @return
     */
    // Ideally replace with wrapWithPostProcessor
    // ISSUE: With the connection interface we cannot mutate the context of update requests
    // Hence the existance of this method is still justified
    public static RDFLink wrapWithContextMutator(RDFLink rawConn, Consumer<Context> contextMutator) {
        RDFLink[] result = {null};

        LinkSparqlUpdate tmp = unwrapUpdateConnection(rawConn);
//        Dataset dataset = tmp instanceof RDFLinkDataset
//                ? getDataset((RDFLinkDataset)tmp)
//                : null;

        result[0] =
            new RDFLinkModular(rawConn, rawConn, rawConn) {
                public QueryExec query(Query query) {
                    return postProcess(rawConn.query(query));
                }

                @Override
                public QueryExec query(String queryString) {
                    return postProcess(rawConn.query(queryString));
                }


                @Override
                public void update(UpdateRequest update) {
                    rawConn.update(update);
//			        checkOpen();
//			        Txn.executeWrite(dataset, () -> {
//                        UpdateProcessor tmp = UpdateExecutionFactory.create(update, dataset);
//                        UpdateProcessor up = postProcess(tmp);
//                        up.execute();
//			        });
                }


                public UpdateProcessor postProcess(UpdateProcessor qe) {
                    Context cxt = qe.getContext();
                    if(cxt != null) {
                        cxt.set(CONNECTION_SYMBOL, result[0]);
                        contextMutator.accept(cxt);
                    }

                    return qe;
                }

                public QueryExec postProcess(QueryExec qe) {
                    Context cxt = qe.getContext();
                    if(cxt != null) {
                        cxt.set(CONNECTION_SYMBOL, result[0]);
                        contextMutator.accept(cxt);
                    }

                    return qe;
                }
            };

        return result[0];
    }

    public static RDFLink wrapWithQueryTransform(RDFLink conn, Function<? super Query, ? extends Query> fn) {
        LinkSparqlQuery queryLink = unwrapQueryConnection(conn);
        LinkSparqlUpdate updateLink = unwrapUpdateConnection(conn);
        LinkDatasetGraph dgLink = unwrapDatasetConnection(conn);

        RDFLink result = new RDFLinkModular(
                new LinkSparqlQueryTransform(queryLink, fn), updateLink, dgLink);

        return result;
    }


}
