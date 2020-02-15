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
    private volatile Throwable interrupt;

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
        this.stream = Stream.generate(() -> get())
                .limit(count)
                .filter(v -> v != UNDEFINED);
    }

    /**
     * Get a next value
     * @return A non-null value
     * @throws JobException The method throws the exeption on a closed builder or an interrupted request
     */
    @Nonnull
    protected T get() throws JobException {
        if (interrupt == null) try {
            final long restMillis = isOpen() ? timeout.toMillis() - clock.millis() + startMilis : 0;
            final T result = restMillis > 0
                    ? queue.poll(restMillis, TimeUnit.MILLISECONDS)
                    : queue.poll();
            if (result == null) {
                close();
                throw new JobException("Time is over: " + timeout);
            }
            return result;
        } catch (InterruptedException e) {
            if (interrupt == null) {
                interrupt = e;
            }
            close();
            Thread.currentThread().interrupt();
        }
        throw new JobException(interrupt);
    }

    /** Start time countdown */
    @Nonnull
    public Stream<T> stream() {
        if (startMilis == Long.MIN_VALUE) {
            startMilis = clock.millis();
        }
        return stream;
    }

    /** Thread save method to add new item to the result Stream.<br>
     *
     * The method accepts delayed item too if the client do not manage the queue due slow data processing.<br>
     *
     * The method accepts new item in an interupted state.<br>
     */
    public void addAll(@Nonnull final T... items) {
        for (T item : items) {
            add(item);
        }
    }

    /** Thread save method to add new item to the result Stream.<br>
     *
     * The method accepts delayed item too if the client do not manage the queue due slow data processing.<br>
     *
     * The method accepts new item in an interupted state.<br>
     */
    public void add(@Nullable final T item) {
        if (countDown.decrementAndGet() >= 0) {
            if (interrupt == null) {
                if (isOpen()) {
                    queue.add(item != null ? item : (T) UNDEFINED);
                } else {
                    throw new JobException("The builder is closed");
                }
            }
        } else {
            throw new JobException("The parameter is over limit: " + item);
        }
    }

    /** Close the builder */
    final protected void close() {
        closed = true;
    }

    /** Returns an open state of the builder */
    final protected boolean isOpen() {
        return !closed;
    }

    /** Interrupt the next processing and close input */
    public void interrupt(@Nonnull final Throwable causedBy) {
        if (interrupt == null) {
            interrupt = Assert.notNull(causedBy, "causedBy");
            close();
            Thread.currentThread().interrupt();
        }
    }

}
