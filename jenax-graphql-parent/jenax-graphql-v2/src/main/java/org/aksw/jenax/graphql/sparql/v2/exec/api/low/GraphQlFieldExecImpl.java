package org.aksw.jenax.graphql.sparql.v2.exec.api.low;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.aksw.jenax.graphql.sparql.v2.acc.state.api.impl.AccStateDriver;
import org.aksw.jenax.graphql.sparql.v2.io.ObjectNotationWriter;
import org.aksw.jenax.graphql.sparql.v2.util.BindingRemapped;
import org.aksw.jenax.graphql.sparql.v2.util.ExecutionContextUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.function.FunctionEnv;

import graphql.language.DirectivesContainer;

public class GraphQlFieldExecImpl<K>
    implements GraphQlFieldExec<K>
{
    protected final Query query;
    protected final QueryExec queryExec;
    protected final RowSet rs;
    protected FunctionEnv functionEnv;
    protected boolean isSingle;
    protected Map<?, Map<Var, Var>> stateVarMap;
    protected AccStateDriver<Binding, FunctionEnv, K, Node> driver;

    protected QueryMapping<K> queryMapping;

    protected boolean isFinished = false;

    /**
     *
     * @param isSingle
     * @param query The query. We don't rely on QueryExec to support accessing the query.
     * @param queryExec
     * @param stateVarMap
     * @param driver
     */
    public GraphQlFieldExecImpl(boolean isSingle, Query query, QueryExec queryExec, Map<?, Map<Var, Var>> stateVarMap, AccStateDriver<Binding, FunctionEnv, K, Node> driver,
            QueryMapping<K> queryMapping) {
        super();
        this.isSingle = isSingle;
        this.query = query;
        this.queryExec = Objects.requireNonNull(queryExec);
        this.rs = Objects.requireNonNull(queryExec.select());
        this.functionEnv = ExecutionContextUtils.createFunctionEnv(); // XXX Ideally use the queryExec's query execution context.
        this.stateVarMap = Objects.requireNonNull(stateVarMap);
        this.driver = Objects.requireNonNull(driver);
        this.queryMapping = queryMapping;
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
                // XXX We could avoid creating a binding wrapper if we had Adapter.getNode(originalBinding, originalToEnum, var);
                Binding mappedBinding = BindingRemapped.of(binding, originalToEnum);

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
    public void writeExtensions(ObjectNotationWriter<K, Node> writer, Function<String, K> stringToKey) throws IOException {
        graphql.language.Node<?> graphQlNode = queryMapping.fieldRewrite().graphQlNode();
        if (graphQlNode instanceof DirectivesContainer<?> container) {
            if (container.hasDirective("debug")) {
                writer.name(stringToKey.apply("metadata"));
                writer.beginObject();
                writer.name(stringToKey.apply("sparqlQuery"));
                writer.value(NodeFactory.createLiteralString(Objects.toString(query)));
                writer.endObject();
            }
        }
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
