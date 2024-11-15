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

import org.aksw.jenax.dataaccess.sparql.builder.exec.update.UpdateExecBuilderCustomBase;
import org.aksw.jenax.dataaccess.sparql.exec.update.DatasetExecWrapperTxn;
import org.aksw.jenax.dataaccess.sparql.execution.update.UpdateEngineFactoryProvider;
import org.aksw.jenax.dataaccess.sparql.factory.execution.update.UpdateProcessorFactoryDataset;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Timeouts.Timeout;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.exec.UpdateExecDataset;
import org.apache.jena.sparql.exec.UpdateExecDatasetBuilder;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.util.Context;

/**
 * Similar to jena's {@link UpdateExecDatasetBuilder} but allows for setting the {@link UpdateEngineFactoryProvider}.
 */
public class UpdateExecDatasetBuilderEx<T extends UpdateExecDatasetBuilderEx<T>> extends UpdateExecBuilderCustomBase<T> {

    protected DatasetGraph dataset;
    protected UpdateEngineFactoryProvider updateEngineFactoryProvider;

    public UpdateExecDatasetBuilderEx() {
        super();
    }

    public UpdateExecDatasetBuilderEx(DatasetGraph dataset, UpdateEngineFactoryProvider updateEngineFactoryProvider) {
        this();
        dataset(dataset);
        updateEngineFactoryProvider(updateEngineFactoryProvider);
    }

    public T dataset(DatasetGraph dsg) {
        this.dataset = dsg;
        return self();
    }

    public T updateEngineFactoryProvider(UpdateEngineFactoryProvider updateEngineFactoryProvider) {
        this.updateEngineFactoryProvider = updateEngineFactoryProvider;
        return self();
    }

    @Override
    public UpdateExec build() {
        Context cxt = contextAccumulator != null ? contextAccumulator.context() : ARQ.getContext();

        UpdateEngineFactory f = updateEngineFactoryProvider.find(dataset, cxt);
        if (f == null) {
            Log.warn(UpdateProcessorFactoryDataset.class, "Failed to find a UpdateEngineFactory for update: " + updateRequest) ;
            return null ;
        }

        // Merge the contexts
        Context finalCxt = Context.setupContextForDataset(cxt, dataset);
        Binding initialBinding = BindingRoot.create();
        UpdateExec result = new UpdateExecDataset(updateRequest, dataset, initialBinding, finalCxt, f, Timeout.UNSET) {};

        // FIXME The Txn wrapper is far not ideal here I suppose?
        // UpdateExec result = new DatasetExecWrapperTxn<>(tmp, dataset);
        return result;
    }
}

//dataset.begin(ReadWrite.WRITE);
//    QueryExecutionBase tmp = new QueryExecutionBase(query, dataset, context, f) ;
//    QueryExecution result = new QueryExecutionDecoratorTxn<QueryExecution>(tmp, dsg);
//    return result;
// UpdateEngine updateEngine = f.create(dsg, null, context);
// Abbreviated forms
//public void execute(DatasetGraph dsg) {
//    dataset(dsg);
//    execute();
//}
//
//private void add(UpdateRequest request) {
//    request.getOperations().forEach(this::add);
//}
//
//private void add(Update update) {
//    this.updateRequest.add(update);
//}

// UpdateProcessorBase tmp = new UpdateProcessorBase(updateRequest, dataset, initialBinding, context, f);
// UpdateProcessor result = UpdateProcessorDecoratorTxn.wrap(tmp, dataset);
// UpdateProcessor result = updateProcessorFactory.create(updateRequest, dataset, context);

//Objects.requireNonNull(dataset, "No dataset for update");
//Objects.requireNonNull(updateRequest, "No update request");
//
//UpdateRequest actualUpdate = updateRequest;
//
//if ( substitutionMap != null && ! substitutionMap.isEmpty() )
//    actualUpdate = UpdateTransformOps.transform(actualUpdate, substitutionMap);
//
//Context cxt = Context.setupContextForDataset(context, dataset);
//UpdateEngineFactory f = UpdateEngineRegistry.get().find(dataset, cxt);
//if ( f == null )
//    throw new UpdateException("Failed to find an UpdateEngine");
//UpdateExec uExec = new UpdateExecDataset(actualUpdate, dataset, initialBinding, cxt, f);
//return uExec;
