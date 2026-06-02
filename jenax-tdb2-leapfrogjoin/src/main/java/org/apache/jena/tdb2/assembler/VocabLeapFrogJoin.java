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

package org.apache.jena.tdb2.assembler;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.assembler.AssemblerUtils;
import org.apache.jena.tdb2.TDB2;

/**
 * RDF vocabulary and registration for the leap frog join dataset assembler.
 * <p>
 * Defines the {@code tdb:DatasetTDBLeapFrog} type and registers
 * {@link DatasetAssemblerLeapFrogJoin} with the global assembler group
 * when {@link #init()} is called (normally triggered automatically
 * by {@link org.apache.jena.tdb2.sys.InitLeapFrogJoin}).
 */
public class VocabLeapFrogJoin {

    private static final String NS = TDB2.namespace;
    public static String getURI() { return NS; }

    /** RDF type for a TDB2 dataset with leap frog join enabled. */
    private static final Resource tDatasetTDBLeapFrog = ResourceFactory.createResource(NS + "DatasetTDBLeapFrog");;

    public static Resource gettDatasetTDBLeapFrog() {
        return tDatasetTDBLeapFrog;
    }

    /**
     * Initialize the assembler registration. Safe to call multiple times.
     */
    public static void init() {
        AssemblerUtils.registerDataset(tDatasetTDBLeapFrog, new DatasetAssemblerLeapFrogJoin());
    }
}
