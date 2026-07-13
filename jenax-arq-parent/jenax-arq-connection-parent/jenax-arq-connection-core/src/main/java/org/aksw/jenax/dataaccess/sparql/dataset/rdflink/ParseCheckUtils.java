package org.aksw.jenax.dataaccess.sparql.dataset.rdflink;

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

import java.util.Optional;

import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.ContextAccumulator;
import org.apache.jena.sparql.util.Symbol;

/**
 * Helper methods to compute the effective value of the ARQ parse check option.
 *
 * @see ARQConstants#parseCheck
 */
public class ParseCheckUtils
{
    // private static final Symbol parseCheck = ARQConstants.parseCheck;
    private static final Symbol parseCheck = Symbol.create("parseCheck");

    // ----- Parse Check -----

    public static void setParseCheck(Context cxt, Boolean value) {
        cxt.set(parseCheck, value);
    }

    public static Optional<Boolean> getParseCheck(DatasetGraph dsg) {
        return Optional.ofNullable(dsg).map(DatasetGraph::getContext).flatMap(ParseCheckUtils::getParseCheck);
    }

    public static Optional<Boolean> getParseCheck(Context cxt) {
        return Optional.ofNullable(cxt).map(c -> c.get(parseCheck));
    }

    public static Optional<Boolean> getParseCheck(ContextAccumulator cxtAcc) {
        return Optional.empty();
        // FIXME The ContextAccumulator.get method did not yet make it into Jena. It should allow lookup-by-key without building the whole context.
        // return Optional.ofNullable(cxtAcc).map(ca -> ca.get(parseCheck);
    }

    public static boolean effectiveParseCheck(Boolean parseCheck, Context cxt) {
        return Optional.ofNullable(parseCheck).orElseGet(() -> getParseCheck(cxt).orElse(true));
    }

    public static boolean effectiveParseCheck(Boolean parseCheck, ContextAccumulator cxtAcc) {
        return Optional.ofNullable(parseCheck).orElseGet(() -> getParseCheck(cxtAcc).orElse(true));
    }
}
