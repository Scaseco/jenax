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

package org.aksw.jenax.dataaccess.sparql.dataset.rdflink.assembler;

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.util.Arrays;

import org.aksw.jenax.dataaccess.sparql.dataset.rdflink.DatasetGraphOverRDFLink;
import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.atlas.lib.Creator;
import org.apache.jena.http.HttpEnv;
import org.apache.jena.http.auth.AuthLib;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdflink.RDFLink;
import org.apache.jena.rdflink.RDFLinkHTTP;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.sparql.core.assembler.DatasetAssembler;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.sys.JenaSystem;

public class DatasetAssemblerHTTP extends DatasetAssembler
{
    static { JenaSystem.init(); }


    private static boolean initialized = false;

    static { init(); }

    static public synchronized void init() {
        if ( initialized )
            return;
        registerWith(Assembler.general());
        initialized = true;
    }

    static void registerWith(AssemblerGroup g) {
        // Wire in the assemblers.
        AssemblerUtils.registerAssembler(g, VocabAssemblerHTTP.tDatasetHTTP, new DatasetAssemblerHTTP());
    }

    @Override
    public DatasetGraph createDataset(Assembler a, Resource root) {
        return make(a, root);
    }

    private static String getAsString(Resource r, Property p, String dft) {
        String tmp = GraphUtils.getAsStringValue(r, p);
        return (tmp != null) ? tmp : dft;
    }

    public static DatasetGraph make(Assembler a, Resource root) {
        // Use destination as the default that can be overridden by  specific endpoints.
        String destination = GraphUtils.getAsStringValue(root, VocabAssemblerHTTP.pDestination);

        String queryEndpoint = destination;
        String updateEndpoint = destination;
        String gspEndpoint = destination;

        queryEndpoint  = getAsString(root, VocabAssemblerHTTP.pQueryEndpoint, destination);
        updateEndpoint = getAsString(root, VocabAssemblerHTTP.pUpdateEndpoint, destination);
        gspEndpoint    = getAsString(root, VocabAssemblerHTTP.pGspEndpoint, destination);

        String q = queryEndpoint;
        String u = updateEndpoint;
        String g = gspEndpoint;

        if (q == null && u == null && g == null) {
            throw new AssemblerException(root, "No destination set using any of the properties: " +
                Arrays.asList(VocabAssemblerHTTP.pDestination, VocabAssemblerHTTP.pQueryEndpoint, VocabAssemblerHTTP.pUpdateEndpoint, VocabAssemblerHTTP.pGspEndpoint));
        }

        boolean isBasicAuth = false;
        boolean isBearerAuth = false;
        String user = null;
        String pass = null;
        String token = null;

        HttpClient httpClient = null;

        Resource authConf = GraphUtils.getResourceValue(root, VocabAssemblerHTTP.pAuth);
        if (authConf != null) {
            user = GraphUtils.getStringValue(authConf, VocabAssemblerHTTP.pUser);
            pass = GraphUtils.getStringValue(authConf, VocabAssemblerHTTP.pPass);
            token = GraphUtils.getStringValue(authConf, VocabAssemblerHTTP.pToken);

            isBasicAuth = user != null || pass != null;
            isBearerAuth = token != null;
        }

        if (isBasicAuth && isBearerAuth) {
            throw new AssemblerException(root, "HTTP Auth: Multiple methods specified.");
        }

        if (isBasicAuth) {
            if ((user != null && pass == null)) {
                throw new AssemblerException(root, "HTTP Credentials: Password is null.");
            }

            if ((user == null && pass != null)) {
                throw new AssemblerException(root, "HTTP Credentials: User is null.");
            }

            if (user != null || pass != null) {
                Authenticator auth = AuthLib.authenticator(user, pass);
                httpClient = HttpEnv.httpClientBuilder().authenticator(auth).build();
            }
        }

        if (isBearerAuth) {
            throw new UnsupportedOperationException("Bearer auth not yet implemented.");
        }

        HttpClient h = httpClient;

        Creator<RDFLink> linkCreator = () -> {
            RDFLink link = RDFLinkHTTP.newBuilder()
                .queryEndpoint(q)
                .updateEndpoint(u)
                .gspEndpoint(g)
                .httpClient(h)
                .build();
            return link;
        };

        DatasetGraph dsg = DatasetGraphOverRDFLink.create(linkCreator);

        /*
        <r> rdf:type tdb:DatasetTDB2;
            tdb:location "dir";
            //ja:context [ ja:cxtName "arq:queryTimeout";  ja:cxtValue "10000" ] ;
            tdb:unionGraph true; # or "true"
        */
        AssemblerUtils.mergeContext(root, dsg.getContext());
        return dsg;
    }
}
