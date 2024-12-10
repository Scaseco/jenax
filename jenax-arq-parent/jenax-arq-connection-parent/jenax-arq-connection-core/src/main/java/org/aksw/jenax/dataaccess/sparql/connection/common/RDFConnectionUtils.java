package org.aksw.jenax.dataaccess.sparql.connection.common;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.aksw.jenax.arq.util.binding.QueryIterOverQueryExec;
import org.aksw.jenax.arq.util.dataset.DatasetDescriptionUtils;
import org.aksw.jenax.arq.util.dataset.DynamicDatasetUtils;
import org.aksw.jenax.arq.util.exec.query.QueryExecTransform;
import org.aksw.jenax.arq.util.exec.query.QueryExecUtils;
import org.aksw.jenax.arq.util.exec.query.QueryExecutionUtils;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.dataaccess.sparql.builder.exec.query.QueryExecBuilderTransform;
import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderTransform;
import org.aksw.jenax.dataaccess.sparql.exec.query.RowSetOverQueryExec;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkTransform;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkUtils;
import org.aksw.jenax.dataaccess.sparql.link.common.RDFLinkWrapperWithCloseShield;
import org.aksw.jenax.stmt.core.SparqlStmtTransform;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionLocal;
import org.apache.jena.rdfconnection.RDFDatasetConnection;
import org.apache.jena.rdfconnection.SparqlQueryConnection;
import org.apache.jena.rdfconnection.SparqlUpdateConnection;
import org.apache.jena.rdflink.RDFConnectionAdapter;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkAdapter;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DynamicDatasets;
import org.apache.jena.sparql.core.DynamicDatasets.DynamicDatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIterSingleton;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.QueryExecBuilder;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.http.Service;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class RDFConnectionUtils {

    /** Symbol for placing a connection (TODO supplier?) into an arq context */
    // @Deprecated
    // public static final Symbol CONNECTION_SYMBOL = Symbol.create("http://jsa.aksw.org/connection");

    /**
     * Context symbol for placing a data source into the query execution context (for sub queries).
     * (Alternatively, the execCxt's datasetGraph could be a DatasetGraphSparqlService)
     */
    // public static final Symbol RDF_DATASOURCE = Symbol.create("rdfDataSource");


    public static RDFConnection withCloseShield(RDFConnection conn) {
        return RDFConnectionAdapter.adapt(new RDFLinkWrapperWithCloseShield(RDFLinkAdapter.adapt(conn)));
    }

    public static SparqlQueryConnection unwrapQueryConnection(SparqlQueryConnection conn) {
        SparqlQueryConnection result;
        if(conn instanceof RDFConnectionModular) {
            SparqlQueryConnection tmp = getQueryConnection((RDFConnectionModular)conn);
            result = unwrapQueryConnection(tmp);
        } else {
            result = conn;
        }

        return result;
    }

    public static SparqlUpdateConnection unwrapUpdateConnection(SparqlUpdateConnection conn) {
        SparqlUpdateConnection result;
        if(conn instanceof RDFConnectionModular) {
            SparqlUpdateConnection tmp = getUpdateConnection((RDFConnectionModular)conn);
            result = unwrapUpdateConnection(tmp);
        } else {
            result = conn;
        }

        return result;
    }

    public static RDFDatasetConnection unwrapDatasetConnection(RDFDatasetConnection conn) {
        RDFDatasetConnection result;
        if(conn instanceof RDFConnectionModular) {
            RDFDatasetConnection tmp = getDatasetConnection((RDFConnectionModular)conn);
            result = unwrapDatasetConnection(tmp);
        } else {
            result = conn;
        }

        return result;
    }


    /** Reflective access to an {@link RDFConnectionModular}'s queryConnection. */
    public static SparqlQueryConnection getQueryConnection(RDFConnectionModular conn) {
        SparqlQueryConnection result;
        try {
            Field f = RDFConnectionModular.class.getDeclaredField("queryConnection");
            f.setAccessible(true);
            result = (SparqlQueryConnection)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /** Reflective access to an {@link RDFConnectionModular}'s updateConnection. */
    public static SparqlUpdateConnection getUpdateConnection(RDFConnectionModular conn) {
        SparqlUpdateConnection result;
        try {
            Field f = RDFConnectionModular.class.getDeclaredField("updateConnection");
            f.setAccessible(true);
            result = (SparqlUpdateConnection)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /** Reflective access to an {@link RDFConnectionModular}'s datasetConnection. */
    public static RDFDatasetConnection getDatasetConnection(RDFConnectionModular conn) {
        RDFDatasetConnection result;
        try {
            Field f = RDFConnectionModular.class.getDeclaredField("datasetConnection");
            f.setAccessible(true);
            result = (RDFDatasetConnection)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /** Reflective access to an {@link RDFConnectionModular}'s dataset. */
    public static Dataset getDataset(RDFConnectionLocal conn) {
        Dataset result;
        try {
            Field f = RDFConnectionLocal.class.getDeclaredField("dataset");
            f.setAccessible(true);
            result = (Dataset)f.get(conn);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static RDFConnection wrapWithBuilderTransform(RDFConnection rawConn, QueryExecBuilderTransform queryBuilderTransform, UpdateExecBuilderTransform updateBuilderTransform) {
        return wrapWithLinkTransform(rawConn, link -> RDFLinkUtils.wrapWithBuilderTransform(link, queryBuilderTransform, updateBuilderTransform));
    }


    public static RDFConnection wrapWithContextMutator(RDFConnection rawConn) {
        return wrapWithContextMutator(rawConn, cxt -> {});
    }


    public static RDFConnection wrapWithContextMutator(RDFConnection rawConn, Consumer<Context> contextMutator) {
        RDFLink oldLink = RDFLinkAdapter.adapt(rawConn);
        RDFLink newLink = RDFLinkUtils.wrapWithContextMutator(oldLink, contextMutator);
        RDFConnection result = RDFConnectionAdapter.adapt(newLink);
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
//    public static RDFConnection wrapWithContextMutatorOld(RDFConnection rawConn, Consumer<Context> contextMutator) {
//        RDFConnection[] result = {null};
//
//        SparqlUpdateConnection tmp = unwrapUpdateConnection(rawConn);
//        Dataset dataset = tmp instanceof RDFConnectionLocal
//                ? getDataset((RDFConnectionLocal)tmp)
//                : null;
//
//        result[0] =
//            new RDFConnectionModular(rawConn, rawConn, rawConn) {
//                public QueryExecution query(Query query) {
//                    return postProcess(rawConn.query(query));
//                }
//
//                @Override
//                public QueryExecution query(String queryString) {
//                    return postProcess(rawConn.query(queryString));
//                }
//
//
//                @Override
//                public void update(UpdateRequest update) {
////			        checkOpen();
////			        Txn.executeWrite(dataset, () -> {
//                        UpdateProcessor tmp = UpdateExecutionFactory.create(update, dataset);
//                        UpdateProcessor up = postProcess(tmp);
//                        up.execute();
////			        });
//                }
//
//                public UpdateProcessor postProcess(UpdateProcessor qe) {
//                    Context cxt = qe.getContext();
//                    if(cxt != null) {
//                        cxt.set(RDFLinkUtils.CONNECTION_SYMBOL, result[0]);
//                        contextMutator.accept(cxt);
//                    }
//
//                    return qe;
//                }
//
//                public QueryExecution postProcess(QueryExecution qe) {
//                    Context cxt = qe.getContext();
//                    if(cxt != null) {
//                        cxt.set(RDFLinkUtils.CONNECTION_SYMBOL, result[0]);
//                        contextMutator.accept(cxt);
//                    }
//
//                    return qe;
//                }
//            };
//
//        return result[0];
//    }

    /** Adapt the given connection as a link. Then apply the transform to that link and return the result. */
    public static RDFConnection wrapWithLinkTransform(RDFConnection conn, RDFLinkTransform linkTransform) {
        RDFLink oldLink = RDFLinkAdapter.adapt(conn);
        RDFLink newLink = linkTransform.apply(oldLink);
        RDFConnection result = RDFConnectionAdapter.adapt(newLink);
        return result;
    }

    public static RDFConnection wrapWithQueryTransform(
            RDFConnection conn,
            QueryTransform queryTransform
        ) {
        return wrapWithQueryTransform(conn, queryTransform, null);
    }


    public static RDFConnection wrapWithStmtTransform(RDFConnection conn, SparqlStmtTransform transform) {
        RDFLink oldLink = RDFLinkAdapter.adapt(conn);
        RDFLink newLink = RDFLinkUtils.wrapWithStmtTransform(oldLink, transform);
        RDFConnection result = RDFConnectionAdapter.adapt(newLink);
        return result;
    }

    public static RDFConnection wrapWithQueryTransform(
            RDFConnection conn,
            QueryTransform queryTransform,
            QueryExecTransform queryExecTransform
            ) {
        RDFLink oldLink = RDFLinkAdapter.adapt(conn);
        RDFLink newLink = RDFLinkUtils.wrapWithQueryTransform(oldLink, queryTransform, queryExecTransform);
        return RDFConnectionAdapter.adapt(newLink);
    }

    public static RDFConnection wrapWithUpdateTransform(
            RDFConnection conn,
            Function<? super UpdateRequest, ? extends UpdateRequest> updateTransform,
            BiFunction<? super UpdateRequest, ? super UpdateProcessor, ? extends UpdateProcessor> updateExecTransform
            ) {
        RDFLink oldLink = RDFLinkAdapter.adapt(conn);
        RDFLink newLink = RDFLinkUtils.wrapWithUpdateTransform(oldLink, updateTransform, updateExecTransform);
        return RDFConnectionAdapter.adapt(newLink);
    }

    public static RDFConnection enableRelativeIrisInQueryResults(RDFConnection delegate) {
        return wrapWithLinkTransform(delegate, RDFLinkUtils::enableRelativeIrisInQueryResults);
    }

    public static RDFConnection wrapWithQueryOnly(RDFConnection conn) {
        return wrapWithLinkTransform(conn, RDFLinkUtils::wrapWithQueryOnly);
    }

    public static RDFConnection wrapWithAutoDisableReorder(RDFConnection conn) {
        return RDFConnectionUtils.wrapWithQueryTransform(conn, null, qe -> {
            QueryExecutionUtils.wrapWithAutoDisableReorder(qe.getQuery(), qe.getContext());
            return qe;
        });
    }

    /**
     * Runs an OpService on the given connection.
     * Resulting bindings must be compatible with the given one and will be merged with it.
     *
     * @param binding The parent binding that is merged with the bindings obtained from the execution.
     * @param execCxt The execution context
     * @param opService The service operation to delegate to the RDFConnection. Its serviceNode is ignored, but silent is repsected.
     * @param isStreamingAllowed If true then a live iterator over the result is returned.
     *          Otherwise the method materializes the data before returning.
     *          If opService.isSilent() is true then isStreaming has no effect because
     *          all data must be materialized first in order to detect errors.
     * @param applyDatasetDescription If true, then check if the execCxt's datasat is a {@link DynamicDatasets} and apply its
     *          default and named graph IRIs to the generated query.
     *
     * @implNote
     *   This method calls {@link #execService(OpService, RDFConnection)}.
     */
    public static QueryIterator execService(Binding binding, ExecutionContext execCxt, OpService opService, RDFConnection target, boolean isStreamingAllowed, boolean applyDatasetDescription) {
        DatasetDescription dd = null;
        if (applyDatasetDescription) {
            DatasetGraph dg = execCxt.getDataset();
            if (dg != null) {
                DynamicDatasetGraph ddg = DynamicDatasetUtils.asUnwrappableDynamicDatasetOrNull(dg);
                if (ddg != null) {
                    dd = DatasetDescriptionUtils.ofNodes(ddg.getOriginalDefaultGraphs(), ddg.getOriginalNamedGraphs());
                }
            }
        }
        QueryIterator qIter = execService(opService, target, isStreamingAllowed, dd);
        qIter = QueryIter.makeTracked(qIter, execCxt);
        return new QueryIterCommonParent(qIter, binding, execCxt);
    }

    /**
     * Runs an OpService on the given connection. Ignores the service IRI but considers the silent flag.
     *
     * @implNote
     *   Adapted from {@link Service#exec(OpService, Context)}.
     */
    public static QueryIterator execService(OpService opService, RDFConnection target, boolean isStreamingAllowed, DatasetDescription datasetDescription) {
        boolean isSilent = opService.getSilent();
        Op opRemote = opService.getSubOp();

        Op opRestored = Rename.reverseVarRename(opRemote, true);
        Query query = OpAsQuery.asQuery(opRestored);

        if (datasetDescription != null) {
            QueryUtils.overwriteDatasetDescription(query, datasetDescription);
        }

        Map<Var, Var> varMapping = QueryExecUtils.computeVarMapping(opRemote, opRestored);

        RDFLink link = RDFLinkAdapter.adapt(target);
        QueryExecBuilder builder = link.newQuery().query(query);

        QueryIterator result = exec(builder, isSilent, isStreamingAllowed);

//        if (silent) {
//        	RDFLink link = RDFLinkAdapter.adapt(target);
//        	Context cxt = link.newQuery().getContext();
//
//            ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(cxt);
//            DataBag<Binding> db = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.bindingSerializationFactory());
//            Iterator<Binding> bindingIt = bindingFlow.blockingIterable().iterator();
//            db.addAll(bindingIt);
//            db.iterator()
//
//            // Stream<Binding> bindingStream = Streams.stream(db.iterator()).onClose(db::close);
//            // bindingFlow = Flowable.fromStream(bindingStream);
//        }


        if (varMapping != null) {
            result = QueryIter.map(result, varMapping);
        }
        return result;
    }

    /**
     * Obtain a RowSet from the given queryExecBuilder.
     * The RowSet is materialized when the silent flag is set or streaming is disallowed.
     * Otherwise it is streamed.
     */
    public static QueryIterator exec(QueryExecBuilder builder, boolean isSilent, boolean isStreamingAllowed) {
        QueryIterator result;
        if (isSilent || !isStreamingAllowed) {
            // Non-streaming case
            try (QueryExec qe = builder.build()) {
                // Detach from the network stream.
                RowSet rs = qe.select();
                RowSet mat = rs.materialize();
                result = QueryIterPlainWrapper.create(mat);
            } catch (RuntimeException ex) {
                if (isSilent) {
                    // logger.warn("SERVICE " + NodeFmtLib.strTTL(opService.getService()) + " : " + ex.getMessage(), ex);
                    // Return the input
                    // result = RowSetStream.create(List.of(), Collections.emptyIterator());
                    result = QueryIterSingleton.create(BindingFactory.root(), null);
                } else {
                    ex.addSuppressed(new RuntimeException("QueryExecution error"));
                    throw ex;
                }
            }
        } else {
            // Streaming case
            // result = new RowSetOverQueryExec(builder.build());
            QueryExec queryExec = builder.build();
            result = new QueryIterOverQueryExec(null, queryExec);
        }
        return result;
    }
}
