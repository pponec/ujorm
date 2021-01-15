/*
 * Copyright 2021-2021 Pavel Ponec
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
package org.ujorm.tools.common;

import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A joinable function
 * 
 * @author Pavel Ponec 
 */
public interface Joinable<D, R> extends Function<D, R> {
    
    /** Send a result of the first function to the next one.
     * @param <T> A middle type
     * @param next Next function
     * @return If a result of the first function is {@code null} than the final result is {@code null} too.
     */
    @Nullable
    default <T> Joinable<D, T> add(@Nonnull final Joinable<R,T> next) {
        return (D t) -> {
            final R value = apply(t);
            return value != null
                    ? next.apply(value)
                    : null;
        };
    }
    
    /** Create a joinable function */
    public static <D, R> Joinable<D, R> of(@Nonnull final Function<D, R> fce) {
        return (D d) -> fce.apply(d); 
    }
}
