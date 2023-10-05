/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aksw.jenax.dataaccess.sparql.factory.dataset.connection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.aksw.jenax.dataaccess.sparql.exec.update.DatasetExecDecoratorTxn;
import org.aksw.jenax.dataaccess.sparql.execution.update.UpdateEngineFactoryProvider;
import org.aksw.jenax.dataaccess.sparql.factory.execution.update.UpdateProcessorFactoryDataset;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecBuilder;
import org.apache.jena.sparql.exec.UpdateExecDataset;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/** Copy of UpdateExecDatasetBuilder because of private ctor - we can't just override the build() method */
public class UpdateExecDatasetBuilderEx implements UpdateExecBuilder {

    // public static UpdateExecDatasetBuilderEx create() { return new UpdateExecDatasetBuilderEx(); }

    private DatasetGraph dataset            = null;
    private Query        query              = null;
    private Context      context            = null;
    // Uses query rewrite to replace variables by values.
    private Map<Var, Node>  substitutionMap  = null;

    private Binding      initialBinding     = null;
    private UpdateRequest update            = null;
    private UpdateRequest updateRequest     = new UpdateRequest();

    protected UpdateEngineFactoryProvider updateEngineFactoryProvider;


    public UpdateExecDatasetBuilderEx(UpdateEngineFactoryProvider updateEngineFactoryProvider) {
        super();
        this.updateEngineFactoryProvider = updateEngineFactoryProvider;
    }

    /** Append the updates in an {@link UpdateRequest} to the {@link UpdateRequest} being built. */
    @Override
    public UpdateExecDatasetBuilderEx update(UpdateRequest updateRequest) {
        Objects.requireNonNull(updateRequest);
        add(updateRequest);
        return this;
    }

    /** Add the {@link Update} to the {@link UpdateRequest} being built. */
    @Override
    public UpdateExecDatasetBuilderEx update(Update update) {
        Objects.requireNonNull(update);
        add(update);
        return this;
    }

    /** Parse and update operations to the {@link UpdateRequest} being built. */
    @Override
    public UpdateExecDatasetBuilderEx update(String updateRequestString) {
        UpdateRequest more = UpdateFactory.create(updateRequestString);
        add(more);
        return this;
    }

    public UpdateExecDatasetBuilderEx dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return this;
    }

    /** Set the {@link Context}.
     *  This defaults to the global settings of {@code ARQ.getContext()}.
     *  If there was a previous call of {@code context} the multiple contexts are merged.
     * */
    @Override
    public UpdateExecDatasetBuilderEx context(Context context) {
        if ( context == null )
            return this;
        ensureContext();
        this.context.putAll(context);
        return this;
    }

    @Override
    public UpdateExecDatasetBuilderEx set(Symbol symbol, Object value) {
        ensureContext();
        this.context.set(symbol, value);
        return this;
    }

    @Override
    public UpdateExecDatasetBuilderEx set(Symbol symbol, boolean value) {
        ensureContext();
        this.context.set(symbol, value);
        return this;
    }


    private void ensureContext() {
        if ( context == null )
            context = new Context();
    }

    @Override
    public UpdateExecDatasetBuilderEx substitution(Binding binding) {
        ensureSubstitutionMap();
        binding.forEach(this.substitutionMap::put);
        return this;
    }

    @Override
    public UpdateExecDatasetBuilderEx substitution(Var var, Node value) {
        ensureSubstitutionMap();
        this.substitutionMap.put(var, value);
        return this;
    }

    private void ensureSubstitutionMap() {
        if ( substitutionMap == null )
            substitutionMap = new HashMap<>();
    }

    public UpdateExecDatasetBuilderEx initialBinding(Binding initialBinding) {
        this.initialBinding = initialBinding;
        return this;
    }

    @Override
    public UpdateExec build() {
        if ( context == null )
            context = ARQ.getContext();  // .copy done in QueryExecutionBase -> Context.setupContext.

        UpdateEngineFactory f = updateEngineFactoryProvider.find(dataset, context);
        if ( f == null )
        {
            Log.warn(UpdateProcessorFactoryDataset.class, "Failed to find a UpdateEngineFactory for update: " + updateRequest) ;
            return null ;
        }
        //dataset.begin(ReadWrite.WRITE);
    //    QueryExecutionBase tmp = new QueryExecutionBase(query, dataset, context, f) ;
    //    QueryExecution result = new QueryExecutionDecoratorTxn<QueryExecution>(tmp, dsg);
    //    return result;
        // UpdateEngine updateEngine = f.create(dsg, null, context);

        // Merge the contexts
        Context cxt = Context.setupContextForDataset(context, dataset);

        Binding initialBinding = BindingRoot.create();

        // UpdateProcessorBase tmp = new UpdateProcessorBase(updateRequest, dataset, initialBinding, context, f);
        UpdateExec tmp = new UpdateExecDataset(updateRequest, dataset, initialBinding, cxt, f) {};
        // UpdateProcessor result = UpdateProcessorDecoratorTxn.wrap(tmp, dataset);
        UpdateExec result = new DatasetExecDecoratorTxn<>(tmp, dataset);


        // UpdateProcessor result = updateProcessorFactory.create(updateRequest, dataset, context);
        return result;

//        Objects.requireNonNull(dataset, "No dataset for update");
//        Objects.requireNonNull(updateRequest, "No update request");
//
//        UpdateRequest actualUpdate = updateRequest;
//
//        if ( substitutionMap != null && ! substitutionMap.isEmpty() )
//            actualUpdate = UpdateTransformOps.transform(actualUpdate, substitutionMap);
//
//        Context cxt = Context.setupContextForDataset(context, dataset);
//        UpdateEngineFactory f = UpdateEngineRegistry.get().find(dataset, cxt);
//        if ( f == null )
//            throw new UpdateException("Failed to find an UpdateEngine");
//        UpdateExec uExec = new UpdateExecDataset(actualUpdate, dataset, initialBinding, cxt, f);
//        return uExec;
    }

    // Abbreviated forms

    @Override
    public void execute() {
        build().execute();
    }

    public void execute(DatasetGraph dsg) {
        dataset(dsg);
        execute();
    }

    private void add(UpdateRequest request) {
        request.getOperations().forEach(this::add);
    }

    private void add(Update update) {
        this.updateRequest.add(update);
    }
}
