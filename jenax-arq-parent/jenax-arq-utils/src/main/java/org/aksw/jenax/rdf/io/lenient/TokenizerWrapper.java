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

import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.Tokenizer;

public interface TokenizerWrapper
    extends  Tokenizer
{
    Tokenizer getDelegate();

    @Override
    default void close() {
        getDelegate().close();
    }

    @Override
    default boolean hasNext() {
        return getDelegate().hasNext();
    }

    @Override
    default Token next() {
        return getDelegate().next();
    }

    @Override
    default Token peek() {
        return getDelegate().peek();
    }

    @Override
    default boolean eof() {
        return getDelegate().eof();
    }

    @Override
    default long getLine() {
        return getDelegate().getLine();
    }

    @Override
    default long getColumn() {
        return getDelegate().getColumn();
    }
}
