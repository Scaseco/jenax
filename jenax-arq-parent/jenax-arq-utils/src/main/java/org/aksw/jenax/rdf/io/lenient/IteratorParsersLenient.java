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

package org.aksw.jenax.rdf.io.lenient;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.jena.atlas.io.PeekReader;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.lang.LangNQuads;
import org.apache.jena.riot.lang.LangNTriples;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerText;
import org.apache.jena.sparql.core.Quad;

public class IteratorParsersLenient {
    /** Create an iterator for parsing N-Triples. */
    public static Iterator<Triple> createIteratorNTriples(InputStream input) {
        return createIteratorNTriples(input, RiotLib.dftProfile());
    }

    /** Create an iterator for parsing N-Triples. */
    public static Iterator<Triple> createIteratorNTriples(InputStream input, ParserProfile profile) {
        PeekReader peekReader = PeekReader.makeUTF8(input);
        Tokenizer core = TokenizerText.create().source(peekReader).errorHandler(profile.getErrorHandler()).build();
        Tokenizer tokenizer = new TokenizerWrapperRecovering(core, peekReader);

        // LangNTriples supports iterator use.
        Iterator<Triple> ct = createParserNTriples(tokenizer, null, profile);
        return new IteratorRecovering<>(ct, peekReader);
    }

    /*package*/ static LangNTriples createParserNTriples(Tokenizer tokenizer, StreamRDF dest, ParserProfile profile) {
        LangNTriples parser = new LangNTriples(tokenizer, profile, dest);
        return parser;
    }

    /** Create an iterator for parsing N-Quads. */
    public static Iterator<Quad> createIteratorNQuads(InputStream input) {
        return createIteratorNQuads(input, RiotLib.dftProfile());
    }

    /**
     * Create an iterator for parsing N-Quads.
     */
    public static Iterator<Quad> createIteratorNQuads(InputStream input, ParserProfile profile) {
        // LangNQuads supports iterator use.
        PeekReader peekReader = PeekReader.makeUTF8(input);
        Tokenizer core = TokenizerText.create().source(peekReader).errorHandler(profile.getErrorHandler()).build();
        Tokenizer tokenizer = new TokenizerWrapperRecovering(core, peekReader);

        // LangNTriples supports iterator use.
        Iterator<Quad> ct = createParserNQuads(tokenizer, null, profile);
        return new IteratorRecovering<>(ct, peekReader);
    }

    /*package*/ static LangNQuads createParserNQuads(Tokenizer tokenizer, StreamRDF dest, ParserProfile profile) {
        LangNQuads parser = new LangNQuads(tokenizer, profile, dest);
        return parser;
    }
}
