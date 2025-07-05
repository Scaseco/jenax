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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

import org.apache.jena.atlas.io.PeekReader;
import org.apache.jena.atlas.iterator.IteratorWrapper;

/** Recovering peek iterator. */
public class IteratorRecovering<T>
    extends IteratorWrapper<T>
{
    protected PeekReader peekReader;

    protected boolean isSlotSet = false;
    protected T slot = null;

    public IteratorRecovering(Iterator<T> iterator, PeekReader peekReader) {
        super(iterator);
        this.peekReader = peekReader;
    }

    /** This method peeks the next valid token. */
    @Override
    public boolean hasNext() {
        boolean result;
        if (isSlotSet) {
            result = true;
        } else {
            while (true) {
                try {
                    result = super.hasNext();
                    if (result) {
                        slot = super.next();
                        isSlotSet = true;
                    }
                    break;
                } catch (Exception e) {
                    TokenizerWrapperRecovering.tryRecovery(peekReader);
                }
            }
        }
        return result;
    }

    @Override
    public T next() {
        hasNext();
        if (!isSlotSet) {
            throw new NoSuchElementException();
        }
        isSlotSet = false;
        return slot;
    }

    // Must override this method in order to use the machinery of this class.
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        while (hasNext()) {
            action.accept(next());
        }
    }
}
