package org.aksw.jenax.arq.util.exec.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.aksw.jenax.arq.util.dataset.DynamicDatasetUtils;
import org.aksw.jenax.arq.util.node.VarScopeUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.algebra.OpVars;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DynamicDatasets.DynamicDatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.QueryEngineRegistry;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.Rename;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.iterator.QueryIter;
import org.apache.jena.sparql.engine.iterator.QueryIterCommonParent;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.engine.iterator.QueryIteratorMapped;
import org.apache.jena.sparql.exec.http.Service;
import org.apache.jena.sparql.util.Context;

public class QueryExecUtils {

    /** Create a QueryIterator from a Stream of items. Also wires up the close method. */
    public static <T> QueryIterator fromStream(Stream<T> stream, Var outVar, Binding parentBinding, ExecutionContext execCxt, java.util.function.Function<? super T, ? extends Node> toNode) {
        QueryIterator result = QueryIterPlainWrapper.create(
                Iter.onClose(
                        stream
                        .map(toNode)
                        .map(n -> BindingFactory.binding(parentBinding, outVar, n))
                        .iterator(),
                        stream::close
                ), execCxt);
        return result;
    }

    /** Create a QueryIterator from a stream of bindings */
    public static QueryIterator fromStream(Stream<Binding> stream, ExecutionContext execCxt) {
        QueryIterator result = QueryIterPlainWrapper.create(Iter.onClose(stream.iterator(), stream::close), execCxt);
        return result;
    }

    /** Special processing that unwraps dynamic datasets */
    public static QueryIterator execute(Op op, DatasetGraph dataset, Binding binding, Context cxt) {
        QueryIterator innerIter = null;
        QueryIterator outerIter = null;
        ExecutionContext execCxt = null;

        DynamicDatasetGraph ddg = DynamicDatasetUtils.asUnwrappableDynamicDatasetOrNull(dataset);
        if (ddg != null) {
            // We are about to create a query from the op which loses scope information
            // Set up the map that allows for mapping the query's result set variables's
            // to the appropriately scoped ones
            Set<Var> visibleVars = OpVars.visibleVars(op);
            Map<Var, Var> normedToScoped = VarScopeUtils.normalizeVarScopes(visibleVars).inverse();

            Op opRestored = Rename.reverseVarRename(op, true);
            Query baseQuery = OpAsQuery.asQuery(opRestored);
            Pair<Query, DatasetGraph> pair = DynamicDatasetUtils.unwrap(baseQuery, ddg);
            Query effQuery = pair.getLeft();
            DatasetGraph effDataset = pair.getRight();

            QueryEngineFactory qef = QueryEngineRegistry.findFactory(effQuery, effDataset, cxt);
            // The scoping of the binding does not match with that of the query.
            // Therefore pass on an empty binding and rename the result set variables
            // back into their proper scope
            Plan plan = qef.create(effQuery, effDataset, BindingFactory.empty(), cxt);
            innerIter = plan.iterator();
            outerIter = new QueryIteratorMapped(innerIter, normedToScoped);
        }

        if (innerIter == null) {
            QueryEngineFactory qef = QueryEngineRegistry.findFactory(op, dataset, cxt);
            Plan plan = qef.create(op, dataset, BindingFactory.empty(), cxt);
            innerIter = plan.iterator();
            outerIter = innerIter;
        }

        execCxt = innerIter instanceof QueryIter ? ((QueryIter)innerIter).getExecContext() : null;
        QueryIterator result = new QueryIterCommonParent(outerIter, binding, execCxt);
        return result;
    }

    /**
     * Computes the variable mapping between the original and restored form of an OpService.
     * The arguments are the subOf of OpService whereas opRestored is the result of
     * {@link Rename#reverseVarRename(Op, boolean)} with argument true.
     * This method is factored out from {@link Service#exec(OpService, Context)}.
     */
    public static Map<Var, Var> computeVarMapping(Op opRemote, Op opRestored) {
        // Op opRemote = opService.getSubOp();
        boolean requiresRemapping = false;
        Map<Var, Var> varMapping = null;
        if ( ! opRestored.equals(opRemote) ) {
            varMapping = new HashMap<>();
            Set<Var> originalVars = OpVars.visibleVars(opRemote); // OpVars.visibleVars(opService);
            Set<Var> remoteVars = OpVars.visibleVars(opRestored);

            for (Var v : originalVars) {
                if (v.getName().contains("/")) {
                    // A variable which was scope renamed so has a different name
                    String origName = v.getName().substring(v.getName().lastIndexOf('/') + 1);
                    Var remoteVar = Var.alloc(origName);
                    if (remoteVars.contains(remoteVar)) {
                        varMapping.put(remoteVar, v);
                        requiresRemapping = true;
                    }
                } else {
                    // A variable which does not have a different name
                    if (remoteVars.contains(v))
                        varMapping.put(v, v);
                }
            }
        }

        Map<Var, Var> result = requiresRemapping ? varMapping : null;
        return result;
    }
}
