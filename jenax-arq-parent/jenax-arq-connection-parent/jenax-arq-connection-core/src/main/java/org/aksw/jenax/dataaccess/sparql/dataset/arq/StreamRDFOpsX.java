/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.aksw.jenax.dataaccess.sparql.dataset.arq;

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFOps;
import org.apache.jena.util.iterator.ExtendedIterator;

/* TODO
 * org.apache.jena.riot.streamrdf?
 *
 */
/** Utilities for sending to StreamRDF.
 *  Unless otherwise stated, send* operations do not call stream.start()/stream.finish()
 *  whereas other operations do.
 */

public class StreamRDFOpsX {
    /**
     * Send the triples of graph and an explicitly given prefix mapping, to a StreamRDF.
     * This operation does not include start/finish nesting - see {@link #graphToStream}.
     */
    public static void sendGraphToStream(Graph graph, StreamRDF stream) {
        PrefixMap prefixMap = PrefixMapFactory.create(graph.getPrefixMapping()) ;
        sendGraphToStream(graph, stream, null, prefixMap) ;
    }

    /** Send the triples of graph, and an explicitly given prefix mapping, to a StreamRDF */
    public static void sendGraphToStream(Graph graph, StreamRDF stream, String baseURI, PrefixMap prefixMap) {
        if ( baseURI != null )
            stream.base(baseURI);
        if ( prefixMap != null )
            StreamRDFOps.sendPrefixesToStream(prefixMap, stream) ;
        sendGraphTriplesToStream(graph, stream);
    }

    /** Send only the triples of graph to a StreamRDF */
    public static void sendGraphTriplesToStream(Graph graph, StreamRDF stream) {
        ExtendedIterator<Triple> iter = graph.find(null, null, null) ;
        try {
            StreamRDFOps.sendTriplesToStream(iter, stream) ;
        } finally { iter.close(); }
    }
}
