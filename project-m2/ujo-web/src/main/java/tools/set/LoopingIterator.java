/*
 *  Copyright 2009-2026 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tools.set;

import org.jetbrains.annotations.NotNull;
import tools.jdbc.RowIterator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An exteded {@link Iterator} is suitable for use in the  {@code for ( ; ; )} statement.
 * @see RowIterator
 * @since 1.86
 * @author Pavel Ponec
 */

public interface LoopingIterator<T> extends Iterator<T>, Iterable<T>, Closeable {

    /**
     * Returns the same object to iterate over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    default Iterator<T> iterator() {
        return this;
    }

    /** Convert to a closeable Stream
     *
     * @see RowIterator class implementation for example how to use
     */
    @NotNull
    default Stream<T> toStream() {
        return StreamSupport.stream(spliterator(), false).onClose(() -> {
            try {
                close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
