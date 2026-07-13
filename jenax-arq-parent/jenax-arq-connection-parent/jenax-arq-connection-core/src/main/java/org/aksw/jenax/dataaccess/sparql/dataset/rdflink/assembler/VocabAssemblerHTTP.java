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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.system.Vocab;

public class VocabAssemblerHTTP
{
    // private static final String NS = "http://jena.apache.org/2025/http#";
    private static final String NS = "https://w3id.org/aksw/jena/dataset#";

    public static String getURI() { return NS; }

    // Types

    // Preferred
    public static final Resource tDatasetHTTP        = Vocab.type(NS, "DatasetHTTP");

    // Property to specify the auth type
    public static final Property pAuth               = Vocab.property(NS, "auth");

    // public static final Resource tAuthBasic        = Vocab.type(NS, "AuthBasic");
    // public static final Resource tAuthBearer        = Vocab.type(NS, "AuthBaerer");

    // Basic auth
    public static final Property pUser               = Vocab.property(NS, "user");
    public static final Property pPass               = Vocab.property(NS, "pass");

    public static final Property pToken               = Vocab.property(NS, "token");

    // Destination sets query, update and gsp endpoint to the same default value.
    // The specific properties can override.
    public static final Property pDestination        = Vocab.property(NS, "destination");
    public static final Property pQueryEndpoint      = Vocab.property(NS, "queryEndpoint");
    public static final Property pUpdateEndpoint     = Vocab.property(NS, "updateEndpoint");
    public static final Property pGspEndpoint        = Vocab.property(NS, "gspEndpoint");
}
