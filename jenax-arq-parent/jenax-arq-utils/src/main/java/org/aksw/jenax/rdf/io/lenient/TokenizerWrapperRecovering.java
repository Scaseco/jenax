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

import org.apache.jena.atlas.io.PeekReader;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.Tokenizer;

public class TokenizerWrapperRecovering
    extends TokenizerWrapperBase
{
    protected PeekReader peekReader;

    public TokenizerWrapperRecovering(Tokenizer delegate, PeekReader peekReader) {
        super(delegate);
        this.peekReader = peekReader;
    }

    @Override
    public boolean hasNext() {
        while (true) {
            try {
                return super.hasNext();
            } catch (Exception e) {
                tryRecovery();
            }
        }
    }

    @Override
    public Token next() {
        while (true) {
            try {
                return super.next();
            } catch (Exception e) {
                tryRecovery();
            }
        }
    }

    protected void tryRecovery() {
        tryRecovery(peekReader);
    }

    public static void tryRecovery(PeekReader peekReader) {
        // // Nothing to do it seems.
        // try {
        //    // peekReader.read();
        //    int c;
        //     c = peekReader.peekChar();
        //    // while ((c = peekReader.read()) != '\n' && c != -1) {}
        //     return c;
        // }
        // catch (Exception e1) {
        //     throw new RuntimeException(e1);
        // }
    }
}
