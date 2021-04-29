/*
 * Copyright 2021-2021 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.web;

import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;

/**
 *
 * @author Pavel Ponec
 */
public final class ExceptionProvider {

    /** Provider with no exception */
    @Nonnull
    private static final ExceptionProvider EMPTY = new ExceptionProvider(null);

    @Nullable
    private final Throwable e;

    private ExceptionProvider(@Nullable final Throwable e) {
        this.e = e;
    }

    /**
     * Apply consumer if the exception is a required type (or not null).
     */
    public void catche(@Nonnull final Consumer<Throwable> exceptionConsumer) {
        catche(Throwable.class, exceptionConsumer);
    }

    /**
     * Apply consumer if the exception is not null.
     */
    public <T extends Throwable> void catche(@Nonnull final Class<T> exceptionClass, @Nonnull final Consumer<T> exceptionConsumer) {
        if (exceptionClass.isInstance(e)) {
            exceptionConsumer.accept((T) e);
        } else if (e != null) {
           throw (e instanceof RuntimeException)
                   ? (RuntimeException) e
                   : new IllegalStateException(e);
        }
    }

    /** A factory method */
    @Nonnull
    public static final ExceptionProvider of(@Nonnull final Throwable e) {
        return new ExceptionProvider(Assert.notNull(e, "Exception is required"));
    }

    /** A factory method */
    @Nonnull
    public static final ExceptionProvider of() {
        return EMPTY;
    }

}