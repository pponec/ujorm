/*
 * Copyright 2020-2020 Pavel Ponec
 * Original source of Ujorm framework: https://bit.ly/340mx4T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.thread;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;

/**
 * An Async Stream Builder.
 * Undefined values (@code null) are are filtered out of the stream.
 * @author Pavel Ponec
 */
public class AsyncStreamBuilder<T> {

    private final AtomicInteger countDown;
    private final Duration timeout;
    private final LinkedBlockingQueue<T> queue;
    private final Stream<T> stream;

    /** Builder for default timeout 1 minute */
    public AsyncStreamBuilder(final int limit) {
        this(limit, Duration.ofMinutes(1));
    }

    /**
     * Constructor
     * @param count Count of stream parameters.
     * @param timeout The minimal resolution is milliseconds
     */
    public AsyncStreamBuilder(final int count, @Nonnull final Duration timeout) {
        this.countDown = new AtomicInteger(count);
        this.timeout = Assert.notNull(timeout, "timeout");
        this.queue = new LinkedBlockingQueue<>();
        this.stream = Stream.generate(() -> getValue()).limit(count);
    }

    protected T getValue() {
        try {
            final T result = queue.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (result == null) {
                throw new TimeoutException("Time is over: " + timeout);
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    public Stream<T> stream() {
        return stream;
    }

    /** Thread save method */
    public void addParams(@Nonnull final T... values) {
        for (T value : values) {
            addParam(value);
        }
    }

    /** Thread save method */
    public void addParam(@Nullable final T value) {
        if (countDown.decrementAndGet() >= 0) {
            if (value != null) {
                queue.add(value);
            }
        } else {
            Assert.state(false, "The parameter is over limit: " + value);
        }
    }
}
