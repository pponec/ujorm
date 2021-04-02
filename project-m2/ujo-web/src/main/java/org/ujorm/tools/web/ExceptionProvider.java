/*
 * Copyright 2021 pavel.
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

/**
 *
 * @author Pavel Ponec
 */
public final class ExceptionProvider {

    @Nullable
    private final Throwable e;

    private ExceptionProvider(@Nullable final Throwable e) {
        this.e = e;
    }

    public Throwable getE() {
        return e;
    }

    /**
     * Apply consumer if the exception is not null.
     */
    public void catche(@Nonnull final Consumer<Throwable> exceptionConsumer) {
        if (e != null) {
            exceptionConsumer.accept(e);
        }
    }

    public static final ExceptionProvider of(@Nonnull final Throwable e) {
        return new ExceptionProvider(e);
    }

    public static final ExceptionProvider of() {
        return new ExceptionProvider(null);
    }

}
