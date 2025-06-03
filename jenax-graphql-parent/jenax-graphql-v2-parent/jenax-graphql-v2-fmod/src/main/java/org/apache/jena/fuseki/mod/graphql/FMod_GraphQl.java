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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.aksw.jenax.graphql.sparql.v2.schema.GraphQlSchemaUtils;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNavigator;
import org.aksw.jenax.web.servlet.graphql.GraphQlUi;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.auth.AuthPolicy;
import org.apache.jena.fuseki.main.FusekiServer;
import org.apache.jena.fuseki.main.sys.FusekiAutoModule;
import org.apache.jena.fuseki.server.DataAccessPoint;
import org.apache.jena.fuseki.server.DataAccessPointRegistry;
import org.apache.jena.fuseki.server.DataService;
import org.apache.jena.fuseki.server.DataService.Builder;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.server.Operation;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;

import graphql.language.AstPrinter;
import graphql.language.Document;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class FMod_GraphQl implements FusekiAutoModule {
    public static final String NS = "https://w3id.org/aksw/norse#graphql.";

    public static final String OP_NAME = NS + "fmod.op";

    public static final Symbol SYM_GRAPHQL_SCHEMA_NAVIGATOR = Symbol.create(NS + "graphQlSchemaNavigator");
    public static final Symbol SYM_GRAPHQL_SCHEMA = Symbol.create(NS + "schemaFile");

    // Symbol.create("http://jena.apache.org/spatial#index");

    public static String getGraphQlSchemaFile(Context cxt) {
        return cxt == null ? null : cxt.getAsString(SYM_GRAPHQL_SCHEMA);
    }

    public static void setGraphQlSchemaNavigator(Context cxt, SchemaNavigator schemaNavigator) {
        cxt.set(SYM_GRAPHQL_SCHEMA_NAVIGATOR, schemaNavigator);
    }

    public static SchemaNavigator getGraphQlSchemaNavigator(Context cxt) {
        return cxt == null ? null : cxt.get(SYM_GRAPHQL_SCHEMA_NAVIGATOR);
    }

    public static Operation graphQlQueryOperation = Operation.alloc(OP_NAME, "graphql", "GraphQL query service");

    private static byte[] jsBundleBytes = null;

    public static byte[] loadJsBundle() {
        if (jsBundleBytes == null) {
            synchronized (FMod_GraphQl.class) {
                if (jsBundleBytes == null) {
                    String jsBundleName = "static/graphql/mui/graphql.bundle.js";
                    try {
                        jsBundleBytes = IO2.readResourceAsBytes(GraphQlUi.class, jsBundleName);
                    } catch (IOException e) {
                        throw new FusekiException(e);
                    }
                }
            }
        }
        return jsBundleBytes;
    }

    @Override
    public String name() {
        return "GraphQL query service";
    }

    @Override
    public void start() {
        Fuseki.configLog.info(name() + ": Add GraphQL operation into global registry.");
    }

    @Override
    public void prepare(FusekiServer.Builder builder, Set<String> datasetNames, Model configModel) {
        Fuseki.configLog.info(name() + ": Module adds GraphQL servlet");
        builder.registerOperation(graphQlQueryOperation, new GraphQlQueryService());
    }

    /**
     * The GraphQL endpoint registration happens here because the Fuseki Service Builder
     * is available which we need to register servlets for the UI javascript bundle.
     */
    @Override
    public void configured(FusekiServer.Builder builder, DataAccessPointRegistry dapRegistry, Model configModel) {
        // FusekiAutoModule.super.configured(builder, dapRegistry, configModel);

        List<DataAccessPoint> daps = dapRegistry.accessPoints();
        for (DataAccessPoint dap : daps) {
            configDataAccessPoint(builder, dap, configModel);
        }

        boolean autoRegisterGraphQlEndpointsForQueryEndpoints = false;
        if (autoRegisterGraphQlEndpointsForQueryEndpoints) {
            registerGraphQlEndpointsForSparqlQueryEndpoints(builder, dapRegistry);
        }
    }

    private void registerGraphQlEndpointsForSparqlQueryEndpoints(FusekiServer.Builder builder, DataAccessPointRegistry dapRegistry) {
        List<DataAccessPoint> oldDaps = dapRegistry.accessPoints();
        List<DataAccessPoint> newDaps = new ArrayList<>(oldDaps.size());
        for (DataAccessPoint dap : oldDaps) {
            DataService dataService = dap.getDataService();
            List<Endpoint> queryEndpoints = Optional.ofNullable(dataService.getEndpoints(Operation.Query)).orElse(List.of());

            List<Endpoint> graphQlEndpoints = new ArrayList<>();
            for (Endpoint queryEndpoint : queryEndpoints) {
                String queryEndpointName = queryEndpoint.getName();
                String baseName = queryEndpointName.isBlank() ? "" : queryEndpointName + "-";
                AuthPolicy authPolicy = queryEndpoint.getAuthPolicy();

                Endpoint graphQlEndpoint = Endpoint.create()
                    .operation(graphQlQueryOperation)
                    .endpointName(baseName + "graphql")
                    .authPolicy(authPolicy)
                    .build();

                graphQlEndpoints.add(graphQlEndpoint);
            }

            // create new DataService based on existing one with the endpoint attached
            Builder dsb = DataService.newBuilder(dataService);
            graphQlEndpoints.forEach(dsb::addEndpoint);
            DataService dSrv = dsb.build();
            DataAccessPoint newDap = new DataAccessPoint(dap.getName(), dSrv);
            newDaps.add(newDap);

            // XXX Perhaps search for a graphql schema configured on the dataset context?

            String name = dap.getName();
            registerJsServlet(builder, name);

        }

        // "replace" each DataAccessPoint
        newDaps.forEach(dap -> {
            dapRegistry.remove(dap.getName());
            dapRegistry.register(dap);
        });
    }

    protected void registerJsServlet(FusekiServer.Builder builder, String name) {
        byte[] jsBundleBytes = loadJsBundle();
        String resServletName = name + "/graphql.bundle.js";
        Fuseki.configLog.info(name() + ": Registering " + resServletName);
        builder.addServlet(resServletName, new HttpServletStaticPayload("text/javascript", jsBundleBytes));
    }

    @Override
    public void configDataAccessPoint(DataAccessPoint dap, Model configModel) {
        throw new RuntimeException("Should not be called.");
    }

    public void configDataAccessPoint(FusekiServer.Builder builder, DataAccessPoint dap, Model configModel) {
        FusekiAutoModule.super.configDataAccessPoint(dap, configModel);
        DataService dataService = dap.getDataService();

        List<Endpoint> endpoints = Optional.ofNullable(dataService.getEndpoints(graphQlQueryOperation)).orElse(List.of());
        for (Endpoint endpoint : endpoints) {
            processGraphQlSchema(builder, dap, endpoint);

        }
        String name = dap.getName();
        registerJsServlet(builder, name);
        // configDataAccessPoint(dap, configModel);
    }

    public static void processGraphQlSchema(FusekiServer.Builder builder, DataAccessPoint dap, Endpoint endpoint) {
        Context cxt = endpoint.getContext();
        String graphQlSchemaFile = getGraphQlSchemaFile(cxt);
        TypeDefinitionRegistry graphQlSchema;
        byte[] graphQlSchemaDocBytes;
        try {
            Document graphQlSchemaDoc = graphQlSchemaFile == null
                ? GraphQlSchemaUtils.loadMetaSchema()
                : GraphQlSchemaUtils.loadSchema(graphQlSchemaFile);

            SchemaParser schemaParser = new SchemaParser();
            graphQlSchema = schemaParser.buildRegistry(graphQlSchemaDoc);
            String graphQlSchemaPrettyStr = AstPrinter.printAst(graphQlSchemaDoc);

            graphQlSchemaDocBytes = graphQlSchemaPrettyStr.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String dapName = dap.getName();
        // String baseName = endpoint.getName();
        // String prefix = baseName.isBlank() ? "" : baseName + "-";
        // prefix = "/";
        String servletName = dapName + "/schema.graphql";

        builder.addServlet(servletName, new HttpServletStaticPayload("application/graphql", graphQlSchemaDocBytes));
        SchemaNavigator graphqlSchemaNavigator = SchemaNavigator.of(graphQlSchema);
        setGraphQlSchemaNavigator(cxt, graphqlSchemaNavigator);
    }

    @Override
    public void serverAfterStarting(FusekiServer server) {
        Fuseki.configLog.info(name() + ": Customized server start on port " + server.getHttpPort());
    }
}
