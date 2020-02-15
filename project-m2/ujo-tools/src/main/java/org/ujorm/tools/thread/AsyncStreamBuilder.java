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

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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

    /** A placeholder for an undefined object */
    private static final Object UNDEFINED = new Object();

    private final AtomicLong countDown;
    private final Duration timeout;
    private final LinkedBlockingQueue<T> queue;
    private final Stream<T> stream;
    private final Clock clock;
    private long startMilis = Long.MIN_VALUE;
    private volatile boolean closed;

    /** Builder for default timeout 1 minute */
    public AsyncStreamBuilder(final long count) {
        this(count, Duration.ofMinutes(1));
    }

    /**
     * Constructor
     * @param count Count of stream parameters.
     * @param timeout The minimal resolution is milliseconds
     */
    public AsyncStreamBuilder(final long count, @Nonnull final Duration timeout) {
        this.countDown = new AtomicLong(count);
        this.timeout = Assert.notNull(timeout, "timeout");
        this.queue = new LinkedBlockingQueue<>();
        this.clock = Clock.systemUTC();
        this.stream = Stream.generate(() -> getValue())
                .limit(count)
                .filter(v -> v != UNDEFINED);
    }

    @Nonnull
    private T getValue() {
        try {
            final long restMillis = timeout.toMillis() - clock.millis() + startMilis;
            final T result = restMillis > 0
                    ? queue.poll(restMillis, TimeUnit.MILLISECONDS)
                    : queue.poll();
            if (result == null) {
                closed = true;
                throw new TimeoutException("Time is over: " + timeout);
            }
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /** Start time countdown */
    @Nonnull
    public Stream<T> stream() {
        if (startMilis == Long.MIN_VALUE) {
            startMilis = clock.millis();
        }
        return stream;
    }

    /** Thread save method to add new items to the result Stream */
    public void addAll(@Nonnull final T... values) {
        for (T value : values) {
            add(value);
        }
    }

    /** Thread save method to add new item to the result Stream */
    public void add(@Nullable final T value) {
        Assert.state(!closed, "The buider is closed");
        if (countDown.decrementAndGet() >= 0) {
            queue.add(value != null ? value : (T) UNDEFINED);
        } else {
            Assert.state(false, "The parameter is over limit: " + value);
        }
    }
}
