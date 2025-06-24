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

package org.aksw.jenax.fuseki.mod.graphql;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.aksw.jenax.graphql.sparql.v2.exec.api.high.GraphQlExec;
import org.aksw.jenax.graphql.sparql.v2.exec.api.high.GraphQlExecBuilder;
import org.aksw.jenax.graphql.sparql.v2.exec.api.high.GraphQlExecFactory;
import org.aksw.jenax.graphql.sparql.v2.exec.api.high.GraphQlExecUtils;
import org.aksw.jenax.graphql.sparql.v2.io.GraphQlJsonUtils;
import org.aksw.jenax.graphql.sparql.v2.schema.SchemaNavigator;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.io.IO;
import org.apache.jena.fuseki.FusekiException;
import org.apache.jena.fuseki.server.Endpoint;
import org.apache.jena.fuseki.servlets.BaseActionREST;
import org.apache.jena.fuseki.servlets.HttpAction;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.web.HttpSC;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * GraphQl query service.
 * POST is used to answer graphql queries passed as JSON documents.
 * GET is used to serve the HTML Web page and the config JSON.
 */
public class GraphQlQueryService extends BaseActionREST {
    /**
     * The GET command can serve: the website, the notification stream from task execution
     * and the latest task execution status.
     */
    @Override
    protected void doGet(HttpAction action) {
        String rawCommand = action.getRequestParameter("command");
        String command = Optional.ofNullable(rawCommand).orElse("webpage");
        switch (command) {
        case "webpage": serveWebPage(action); break;
        case "config": serveConfig(action); break;
        default:
            throw new UnsupportedOperationException("Unsupported command (via HTTP GET): " + command);
        }
    }

    /** Get request; currently always returns HTML */
    protected void serveWebPage(HttpAction action) {
        // Serves the minimal graphql ui
        String resourceName = "static/graphql/mui/index.html";
        String str = null;
        try (InputStream in = GraphQlQueryService.class.getClassLoader().getResourceAsStream(resourceName)) {
            str = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FusekiException(e);
        }

        if (str == null) {
            action.setResponseStatus(HttpSC.INTERNAL_SERVER_ERROR_500);
            action.setResponseContentType(WebContent.contentTypeTextPlain);
            str = "Failed to load classpath resource " + resourceName;
        } else {
            action.setResponseStatus(HttpSC.OK_200);
            action.setResponseContentType(WebContent.contentTypeHTML);
        }
        try (OutputStream out = action.getResponseOutputStream()) {
            IOUtils.write(str, out, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FusekiException(e);
        }
    }

    protected void serveConfig(HttpAction action) {
        Endpoint endpoint = action.getEndpoint();
        Context cxt = endpoint.getContext();
        JsonObject jsonObject = new JsonObject();

        String viewerUrl = FMod_GraphQl.getGraphQlSparqlQueryViewerUrl(cxt);
        jsonObject.addProperty("sparqlQueryViewer", viewerUrl);

        String endpointUrl = FMod_GraphQl.getGraphQlSparqlQueryEndpointUrl(cxt);
        jsonObject.addProperty("sparqlQueryEndpoint", endpointUrl);

        String str = new Gson().toJson(jsonObject);
        successJson(action, str);
    }

    protected static void successJson(HttpAction action, String jsonStr) {
        successStringUtf8(action, WebContent.contentTypeJSON, jsonStr);
    }

    protected static void successStringUtf8(HttpAction action, String contentType, String str) {
        action.setResponseContentType(contentType);
        action.setResponseCharacterEncoding(WebContent.charsetUTF8);
        action.setResponseStatus(HttpSC.OK_200);
        try {
            action.getResponseOutputStream().println(str);
        } catch (IOException e) {
            IO.exception(e);
        }
        return;
    }

    /** Post request; currently always handles graphql execution */
    @Override
    protected void doPost(HttpAction action) {
        Context endpointCxt = action.getEndpoint().getContext();
        SchemaNavigator schemaNavigator = FMod_GraphQl.getGraphQlSchemaNavigator(endpointCxt);

        DatasetGraph dsg = action.getDataset();
        // Preconditions.checkArgument(dsg != null, "DatasetGraph not set for request");

        String queryJsonStr;
        try (InputStream in = action.getRequestInputStream()) {
            queryJsonStr = IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e1) {
            throw new FusekiException(e1);
        }

        Gson gson = new Gson();
        JsonObject queryJson = gson.fromJson(queryJsonStr, JsonObject.class);

        GraphQlExec<String> exec;
        GraphQlExecFactory qef = GraphQlExecFactory.of(() -> QueryExec.newBuilder().dataset(dsg));
        GraphQlExecBuilder builder = qef.newBuilder();
        if (schemaNavigator != null) {
            builder = builder.schemaNavigator(schemaNavigator);
        }
        // builder = GraphQlJsonUtils.configureFromJson(builder, query);
        GraphQlJsonUtils.configureFromJson(builder, queryJson);
        exec = builder.buildForJson();

        action.beginRead();
        try {
            action.setResponseStatus(HttpSC.OK_200);
            action.setResponseContentType(WebContent.contentTypeJSON);
            try (OutputStream out = action.getResponseOutputStream()) {
                GraphQlExecUtils.write(out, exec);
                // GraphQlExecUtils.writePretty(out, exec);
            }
            // action.log.info(format("[%d] graphql: execution finished", action.id));
        } catch (IOException e) {
            throw new FusekiException(e);
        } finally {
            action.end();
        }
    }
}
