package org.aksw.jenax.graphql.sparql.v2.api.low;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateDriver;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.aksw.jenax.graphql.sparql.v2.util.BindingRemapped;
import org.aksw.jenax.graphql.sparql.v2.util.ExecutionContextUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.function.FunctionEnv;

public class GraphQlFieldExecImpl<K>
    implements GraphQlFieldExec<K>
{
    protected final QueryExec queryExec;
    protected final RowSet rs;
    protected FunctionEnv functionEnv;
    protected boolean isSingle;
    protected Map<?, Map<Var, Var>> stateVarMap;
    protected AccStateDriver<Binding, FunctionEnv, K, Node> driver;

    protected boolean isFinished = false;

    public GraphQlFieldExecImpl(boolean isSingle, QueryExec queryExec, Map<?, Map<Var, Var>> stateVarMap, AccStateDriver<Binding, FunctionEnv, K, Node> driver) {
        super();
        this.isSingle = isSingle;
        this.queryExec = Objects.requireNonNull(queryExec);
        this.rs = Objects.requireNonNull(queryExec.select());
        this.functionEnv = ExecutionContextUtils.createFunctionEnv(); // XXX Ideally use the queryExec's query execution context.
        this.stateVarMap = Objects.requireNonNull(stateVarMap);
        this.driver = Objects.requireNonNull(driver);
    }

    @Override
    public boolean isSingle() {
        return isSingle;
    }

    /** Get the underlying Jena {@link QueryExec}. */
    public QueryExec getQueryExec() {
        return queryExec;
    }

    @Override
    public boolean sendNextItemToWriter(ObjectNotationWriter<K, Node> writer) throws IOException {
        boolean result;
        driver.getContext().setWriter(writer);

        if (isFinished) {
            result = false;
        } else if (!rs.hasNext()) {
            isFinished = true;
            result = driver.end();
        } else {
            boolean completedObject = false;
            while (rs.hasNext()) {
                Binding binding = rs.next();
                Object state = driver.getInputToStateId().apply(binding, functionEnv);
                Map<Var, Var> originalToEnum = stateVarMap.get(state);
                if (originalToEnum == null) {
                    throw new IllegalStateException("No variable mapping obtained for state: " + state);
                }
                Binding mappedBinding = BindingRemapped.of(binding, originalToEnum);

                // System.out.println(rs.next());
                completedObject = driver.accumulate(mappedBinding, functionEnv);
                if (completedObject) {
                    break;
                }
            }

            if (!completedObject) {
                isFinished = true;
                result = driver.end();
            } else {
                result = true;
            }
        }
        return result;
    }

    @Override
    public void close() {
        queryExec.close();
    }

    @Override
    public void abort() {
        queryExec.abort();
    }
}

//// @Override
//public Object nextItem(GonProvider<K, V> gonProvider) {
//  // Create an in-memory writer over the driver's gonProvider
//  // GonProvider<K, V> gonProvider = driver.getContext().getGonProvider();
//  ObjectNotationWriterViaGon<K, V> tmp = ObjectNotationWriterViaGon.of(gonProvider);
//
//  try {
//      sendNextItemToWriter(tmp);
//  } catch (IOException e) {
//      throw new RuntimeException(e);
//  }
//
//  Object result = tmp.getProduct();
//  return result;
//}
