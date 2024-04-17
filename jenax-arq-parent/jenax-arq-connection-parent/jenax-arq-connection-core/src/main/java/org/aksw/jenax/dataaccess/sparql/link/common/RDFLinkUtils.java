package org.aksw.jenax.dataaccess.sparql.link.common;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.prologue.PrologueUtils;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.update.UpdateRequestTransform;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderWrapperBase;
import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderWrapperBase;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecWithNodeTransform;
import org.aksw.jenax.dataaccess.sparql.exec.query.QueryExecWrapperTxn;
import org.aksw.jenax.dataaccess.sparql.exec.update.UpdateExecWrapperTxn;
import org.aksw.jenax.dataaccess.sparql.link.dataset.LinkDatasetGraphWrapperTxn;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryTransform;
import org.aksw.jenax.dataaccess.sparql.link.query.LinkSparqlQueryWrapperBase;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateRequest;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateUpdateTransform;
import org.aksw.jenax.dataaccess.sparql.link.update.LinkSparqlUpdateWrapperBase;
import org.aksw.jenax.stmt.core.SparqlStmt;
import org.aksw.jenax.stmt.core.SparqlStmtQuery;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.aksw.jenax.stmt.core.SparqlStmtUpdate;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.rdflink.LinkDatasetGraph;
import org.apache.jena.rdflink.LinkSparqlQuery;
import org.apache.jena.rdflink.LinkSparqlUpdate;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkModular;
import org.apache.jena.sparql.core.Transactional;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.modify.request.UpdateLoad;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class RDFLinkUtils {

    /** Symbol for placing a connection (TODO supplier?) into an arq context */
    @Deprecated // Data source is the better abstraction!
    public static final Symbol CONNECTION_SYMBOL = Symbol.create("http://jsa.aksw.org/connection");

    /**
     * A data source for making sub-sparql queries. Care needs to be taken to avoid deadlocks:
     * If the original connection is in a write transaction, then sub-queries may block.
     */
    public static final Symbol symRdfDataSource = Symbol.create("https://w3id.org/aksw/jenax#rdfDataSource");

    public static RDFLink withCloseShield(RDFLink link) {
        return new RDFLinkWrapperWithCloseShield(link);
    }

    public static LinkSparqlQuery unwrapLinkSparqlQuery(LinkSparqlQuery link) {
        LinkSparqlQuery result;
        if(link instanceof RDFLinkModular) {
            LinkSparqlQuery tmp = ((RDFLinkModular)link).queryLink();
            result = unwrapLinkSparqlQuery(tmp);
        } else {
            result = link;
        }
        return result;
    }

    public static LinkSparqlUpdate unwrapLinkSparqlUpdate(LinkSparqlUpdate link) {
        LinkSparqlUpdate result;
        if(link instanceof RDFLinkModular) {
            LinkSparqlUpdate tmp = ((RDFLinkModular)link).updateLink();
            result = unwrapLinkSparqlUpdate(tmp);
        } else {
            result = link;
        }
        return result;
    }

    public static LinkDatasetGraph unwrapLinkDatasetGraph(LinkDatasetGraph link) {
        LinkDatasetGraph result;
        if(link instanceof RDFLinkModular) {
            LinkDatasetGraph tmp = ((RDFLinkModular)link).datasetLink();
            result = unwrapLinkDatasetGraph(tmp);
        } else {
            result = link;
        }
        return result;
    }

    public static RDFLink wrapWithContextMutator(RDFLink rawConn) {
        return wrapWithContextMutator(rawConn, cxt -> {});
    }

    /** Return a new link that applies the given contextMutator whenever a new builder is requested */
    public static LinkSparqlQuery wrapQueryWithContextMutator(LinkSparqlQuery baseLink, Consumer<Context> contextMutator) {
        return new LinkSparqlQueryWrapperBase(baseLink) {
            @Override
            public QueryExecBuilder newQuery() {
                QueryExecBuilder r = baseLink.newQuery();
                Context cxt = new Context();
                contextMutator.accept(cxt);
                for (Symbol key : cxt.keys()) {
                    Object val = cxt.get(key);
                    r.set(key, val);
                }
                return r;
            }
        };
    }

    /** Return a new link that applies the given contextMutator whenever a new builder is requested */
    public static LinkSparqlUpdate wrapUpdateWithContextMutator(LinkSparqlUpdate baseLink, Consumer<Context> contextMutator) {
        return new LinkSparqlUpdateWrapperBase(baseLink) {
            @Override
            public UpdateExecBuilder newUpdate() {
                UpdateExecBuilder r = baseLink.newUpdate();
                Context cxt = new Context();
                contextMutator.accept(cxt);
                cxt.keys().forEach(key -> {
                    Object val = cxt.get(key);
                    r.set(key, val);
                });
                return r;
            }
        };
    }

    /**
     * If the argument is an RDFLinkModular then return it as is
     * otherwise create an RDFLinkModular with the given link in all of its positions.
     */
    public static RDFLinkModular asModular(RDFLink link) {
        RDFLinkModular result = link instanceof RDFLinkModular
            ? (RDFLinkModular)link
            : new RDFLinkModular(link, link, link);
        return result;
    }

    public static RDFLink wrapWithContextMutator(RDFLink link, Consumer<Context> contextMutator) {
        RDFLinkModular tmp = asModular(link);
        RDFLinkModular result = new RDFLinkModular(
                wrapQueryWithContextMutator(tmp.queryLink(), contextMutator),
                wrapUpdateWithContextMutator(tmp.updateLink(), contextMutator),
                tmp.datasetLink());
        return result;
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
//    public static RDFLink wrapWithContextMutatorOld(RDFLink rawConn, Consumer<Context> contextMutator) {
//        RDFLink[] result = {null};
//
//        LinkSparqlUpdate tmp = unwrapLinkSparqlUpdate(rawConn);
////        Dataset dataset = tmp instanceof RDFLinkDataset
////                ? getDataset((RDFLinkDataset)tmp)
////                : null;
//
//        result[0] =
//            new RDFLinkModular(rawConn, rawConn, rawConn) {
//                public QueryExec query(Query query) {
//                    return postProcess(rawConn.query(query));
//                }
//
//                @Override
//                public QueryExec query(String queryString) {
//                    return postProcess(rawConn.query(queryString));
//                }
//
//
//                @Override
//                public void update(UpdateRequest update) {
//                    rawConn.update(update);
////			        checkOpen();
////			        Txn.executeWrite(dataset, () -> {
////                        UpdateProcessor tmp = UpdateExecutionFactory.create(update, dataset);
////                        UpdateProcessor up = postProcess(tmp);
////                        up.execute();
////			        });
//                }
//
//                public UpdateProcessor postProcess(UpdateProcessor qe) {
//                    Context cxt = qe.getContext();
//                    if(cxt != null) {
//                        cxt.set(CONNECTION_SYMBOL, result[0]);
//                        contextMutator.accept(cxt);
//                    }
//
//                    return qe;
//                }
//
//                public QueryExec postProcess(QueryExec qe) {
//                    Context cxt = qe.getContext();
//                    if(cxt != null) {
//                        cxt.set(CONNECTION_SYMBOL, result[0]);
//                        contextMutator.accept(cxt);
//                    }
//
//                    return qe;
//                }
//            };
//
//        return result[0];
//    }

    public static RDFLink wrapWithQueryTransform(
            RDFLink conn,
            QueryTransform queryTransform,
            QueryExecTransform queryExecTransform
            ) {
        LinkSparqlQuery queryLink = unwrapLinkSparqlQuery(conn);
        LinkSparqlUpdate updateLink = unwrapLinkSparqlUpdate(conn);
        LinkDatasetGraph dgLink = unwrapLinkDatasetGraph(conn);

        RDFLink result = new RDFLinkModular(
                new LinkSparqlQueryQueryTransform(queryLink, queryTransform, queryExecTransform), updateLink, dgLink);

        return result;
    }

    public static RDFLink wrapWithUpdateTransform(
            RDFLink conn,
            Function<? super UpdateRequest, ? extends UpdateRequest> updateTransform,
            BiFunction<? super UpdateRequest, ? super UpdateProcessor, ? extends UpdateProcessor> updateExecTransform) {
        LinkSparqlQuery queryLink = unwrapLinkSparqlQuery(conn);
        LinkSparqlUpdate updateLink = unwrapLinkSparqlUpdate(conn);
        LinkDatasetGraph dgLink = unwrapLinkDatasetGraph(conn);

        RDFLink result = new RDFLinkModular(
                queryLink, new LinkSparqlUpdateUpdateTransform(updateLink, updateTransform, updateExecTransform), dgLink);

        return result;
    }

    /**
     * Apply a SparqlStmtTransform to the given link.
     * The transform must not convert query statements to update ones or vice versa.
     *
     * @param conn
     * @param transform
     * @return
     */
    public static RDFLink wrapWithStmtTransform(RDFLink conn, SparqlStmtTransform transform) {
        QueryTransform xformQuery = before -> {
            SparqlStmt beforeStmt = new SparqlStmtQuery(before);
            SparqlStmt afterStmt = transform.apply(beforeStmt);
            Query r = afterStmt.getQuery();
            return r;
        };

        UpdateRequestTransform xformUpdate = before -> {
            SparqlStmt beforeStmt = new SparqlStmtUpdate(before);
            SparqlStmt afterStmt = transform.apply(beforeStmt);
            UpdateRequest r = afterStmt.getUpdateRequest();
            return r;
        };

        LinkSparqlQuery queryLink = unwrapLinkSparqlQuery(conn);
        LinkSparqlUpdate updateLink = unwrapLinkSparqlUpdate(conn);
        LinkDatasetGraph dgLink = unwrapLinkDatasetGraph(conn);

        RDFLink result = new RDFLinkModular(
                new LinkSparqlQueryQueryTransform(queryLink, xformQuery, null),
                new LinkSparqlUpdateUpdateTransform(updateLink, xformUpdate, null),
                dgLink);

        return result;
    }

    public static RDFLink wrapWithLoadViaInsert(RDFLink conn) {
        LinkSparqlQuery lq = unwrapLinkSparqlQuery(conn);
        LinkSparqlUpdate lu = unwrapLinkSparqlUpdate(conn);
        LinkDatasetGraph ld = unwrapLinkDatasetGraph(conn);

        // FIXME Finish the wrapping!!!
        return new RDFLinkModular(lq, lu, ld);
    }

    /** Wrap a link such that {@link UpdateLoad} requests are passed to
     *  the load functions of the LinkDatasetGraph component of the RDFLink */
    public static RDFLink wrapWithLoadViaLinkDatasetGraph(RDFLink conn) {
        LinkSparqlQuery lq = unwrapLinkSparqlQuery(conn);
        LinkSparqlUpdate lu = unwrapLinkSparqlUpdate(conn);
        LinkDatasetGraph ld = unwrapLinkDatasetGraph(conn);

        LinkSparqlUpdate updateLink = new LinkSparqlUpdateRequest() {
            @Override
            public LinkSparqlUpdate getDelegate() {
                return lu;
            }

            @Override
            public void update(UpdateRequest update) {
                // Prologue prologue = update.copy();
                UpdateRequest pending = null;

                for (Update u : update.getOperations()) {
                    if (u instanceof UpdateLoad) {
                        if (pending != null) {
                            lu.update(pending);
                            pending = null;
                        }

                        UpdateLoad load = (UpdateLoad)u;
                        Node dest = load.getDest();
                        String source = load.getSource();

                        if (dest == null) {
                            ld.load(source);
                        } else {
                            ld.load(dest, source);
                        }
                    } else {
                        if (pending != null) {
                            lu.update(pending);
                        }

                        pending = new UpdateRequest();
                        PrologueUtils.copy(pending, update);
                        pending.add(u);
                    }
                }

                if (pending != null) {
                    lu.update(pending);
                }
            }
        };


        RDFLink result = new RDFLinkModular(lq, updateLink, ld);
        return result;
    }

    public static RDFLink wrapWithAutoTxn(RDFLink rdfLink, Transactional transactional) {
        RDFLinkModular mod = asModular(rdfLink);
        return new RDFLinkModular(
            new LinkSparqlQueryWrapperBase(mod.queryLink()) {
                @Override
                public QueryExecBuilder newQuery() {
                    return new QueryExecBuilderWrapperBase(getDelegate().newQuery()) {
                        @Override
                        public QueryExec build() {
                            return QueryExecWrapperTxn.wrap(getDelegate().build(), transactional);
                        }
                    };
                }
            },
            new LinkSparqlUpdateWrapperBase(mod.updateLink()) {
                @Override
                public UpdateExecBuilder newUpdate() {
                    return new UpdateExecBuilderWrapperBase(getDelegate().newUpdate()) {
                        @Override
                        public UpdateExec build() {
                            return UpdateExecWrapperTxn.wrap(getDelegate().build(), transactional);
                        }
                    };
                }
            },
            LinkDatasetGraphWrapperTxn.wrap(mod.datasetLink(), transactional)
        );
    }


    /**
     * Standard sparql does not support creating relative IRIs.
     * The spec states that the IRI function *must* return an absolute IRI.
     *
     * This wrapper sets a dummy base IRI on queries that do not have a base set and post processes result sets such that IRIs with that dummy base
     * have the base removed.
     *
     * @param conn
     */
    public static RDFLink enableRelativeIrisInQueryResults(RDFLink delegate) {
        String dummyBaseUrl = "http://dummy.base/url/";
        int offset = dummyBaseUrl.length();

        NodeTransform xform = node -> {
            Node r = node;
            if (node.isURI()) {
                String str = node.getURI();
                if (str.startsWith(dummyBaseUrl)) {
                    r = NodeFactory.createURI(str.substring(offset));
                }
            }
            return r;
        };

        return wrapWithQueryTransform(delegate,
                query -> {
                    if (!query.explicitlySetBaseURI()) {
                        query = query.cloneQuery();
                        query.setBaseURI(dummyBaseUrl);
                    }
                    return query;
                },
                qe -> new QueryExecWithNodeTransform(qe, xform));
    }

    public static RDFLink apply(RDFLink link, LinkSparqlUpdateTransform transform) {
        LinkSparqlQuery queryLink = unwrapLinkSparqlQuery(link);
        LinkSparqlUpdate updateLink = unwrapLinkSparqlUpdate(link);
        LinkDatasetGraph dgLink = unwrapLinkDatasetGraph(link);
        LinkSparqlUpdate newUpdateLink = transform.apply(updateLink);
        RDFLink result = new RDFLinkModular(queryLink, newUpdateLink, dgLink);
        return result;
    }

    public static RDFLink apply(RDFLink link, LinkSparqlQueryTransform transform) {
        LinkSparqlQuery queryLink = unwrapLinkSparqlQuery(link);
        LinkSparqlUpdate updateLink = unwrapLinkSparqlUpdate(link);
        LinkDatasetGraph dgLink = unwrapLinkDatasetGraph(link);
        LinkSparqlQuery newQueryLink = transform.apply(queryLink);
        RDFLink result = new RDFLinkModular(newQueryLink, updateLink, dgLink);
        return result;
    }

    /** Disable update and dataset APIs */
    public static RDFLink wrapWithQueryOnly(RDFLink link) {
        LinkSparqlQuery queryLink = unwrapLinkSparqlQuery(link);
        return new RDFLinkModular(queryLink, null, null);
    }
}

// The following code seems to be no longer needed as the public getters have been added

/** Reflective access to an {@link RDFLinkModular}'s queryConnection. */
//public static LinkSparqlQuery getQueryConnection(RDFLinkModular conn) {
//  LinkSparqlQuery result;
//  try {
//      Field f = RDFLinkModular.class.getDeclaredField("queryConnection");
//      f.setAccessible(true);
//      result = (LinkSparqlQuery)f.get(conn);
//  } catch(Exception e) {
//      throw new RuntimeException(e);
//  }
//
//  return result;
//}

/** Reflective access to an {@link RDFLinkModular}'s updateConnection. */
//public static LinkSparqlUpdate getUpdateConnection(RDFLinkModular conn) {
//  LinkSparqlUpdate result;
//  try {
//      Field f = RDFLinkModular.class.getDeclaredField("updateConnection");
//      f.setAccessible(true);
//      result = (LinkSparqlUpdate)f.get(conn);
//  } catch(Exception e) {
//      throw new RuntimeException(e);
//  }
//
//  return result;
//}

/** Reflective access to an {@link RDFLinkModular}'s datasetConnection. */
//public static LinkDatasetGraph getDatasetConnection(RDFLinkModular conn) {
//  LinkDatasetGraph result;
//  try {
//      Field f = RDFLinkModular.class.getDeclaredField("datasetConnection");
//      f.setAccessible(true);
//      result = (LinkDatasetGraph)f.get(conn);
//  } catch(Exception e) {
//      throw new RuntimeException(e);
//  }
//
//  return result;
//}

/** Reflective access to an {@link RDFLinkModular}'s dataset. */
//public static Dataset getDataset(RDFLinkDataset conn) {
//  Dataset result;
//  try {
//      Field f = RDFLinkDataset.class.getDeclaredField("dataset");
//      f.setAccessible(true);
//      result = (Dataset)f.get(conn);
//  } catch(Exception e) {
//      throw new RuntimeException(e);
//  }
//  return result;
//}
