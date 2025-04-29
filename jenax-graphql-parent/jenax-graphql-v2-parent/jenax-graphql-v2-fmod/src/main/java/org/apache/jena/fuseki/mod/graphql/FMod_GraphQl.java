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
package org.apache.jena.fuseki.mod.graphql;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.jenax.web.servlet.graphql.GraphQlUi;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiAutoModule;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.rdf.model.Model;

public class FMod_GraphQl implements FusekiAutoModule {

    private Operation graphQlQueryOperation = null;

    @Override
    public String name() {
        return "GraphQL query service";
    }

    @Override
    public void start() {
        Fuseki.configLog.info(name() + ": Add GraphQL operation into global registry.");
        graphQlQueryOperation = Operation.alloc("http://org.apache.jena/graphql-service",
                "graphql",
                "GraphQL query service");
    }

    @Override
    public void prepare(FusekiServer.Builder builder, Set<String> datasetNames, Model configModel) {
        Fuseki.configLog.info(name() + ": Module adds GraphQL servlet");
        builder.registerOperation(graphQlQueryOperation, new GraphQlQueryService());
    }

    @Override
    public void configured(FusekiServer.Builder builder, DataAccessPointRegistry dapRegistry, Model configModel) {
        FusekiAutoModule.super.configured(builder, dapRegistry, configModel);

        List<DataAccessPoint> daps = dapRegistry.accessPoints().stream().map(dap -> {
            Endpoint endpoint = Endpoint.create()
                    .operation(graphQlQueryOperation)
                    .endpointName("graphql")
                    .build();
            // create new DataService based on existing one with the endpoint attached
            DataService dSrv = DataService.newBuilder(dap.getDataService()).addEndpoint(endpoint).build();
            return new DataAccessPoint(dap.getName(), dSrv);
        }).collect(Collectors.toList());

        String jsBundleName = "static/graphql/mui/graphql.bundle.js";
        byte[] jsBundleBytes;
        try {
            jsBundleBytes = IO2.readResourceAsBytes(GraphQlUi.class, jsBundleName);
        } catch (IOException e) {
            throw new FusekiException(e);
        }

        for (DataAccessPoint dap : daps) {
            String name = dap.getName();
            // builder.addEndpoint(name, "graphql", graphQlQueryOperation);
            String resServletName = name + "/graphql.bundle.js";
            Fuseki.configLog.info(name() + ": Registering " + resServletName);
            builder.addServlet(resServletName,  new HttpServletStaticPayload("text/javascript", jsBundleBytes));
        }

        // "replace" each DataAccessPoint
        daps.forEach(dap -> {
            dapRegistry.remove(dap.getName());
            dapRegistry.register(dap);
        });
    }

    @Override
    public void configDataAccessPoint(DataAccessPoint dap, Model configModel) {
        FusekiAutoModule.super.configDataAccessPoint(dap, configModel);
    }

    @Override
    public void serverAfterStarting(FusekiServer server) {
        Fuseki.configLog.info(name() + ": Customized server start on port " + server.getHttpPort());
    }
}
