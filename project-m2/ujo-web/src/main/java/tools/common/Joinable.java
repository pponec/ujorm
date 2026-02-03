/*
 * Copyright 2021-2022 Pavel Ponec
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
package tools.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * A joinable function
 *
 * <h4>Usage</h4>
 * <pre>
 *  Function&lt;Person, String&gt; nameProvider = Joinable
 *     .of (Person::getBoss)
 *     .add(Person::getBoss)
 *     .add(Person::getName);
 *  String superBossName = nameProvider.apply(getPerson());
 * </pre>
 *
 * @author Pavel Ponec
 */
public interface Joinable<D, R> extends Function<D, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param d The {@code nullable} function argument
     * @return The function result
     */
    @Override
    @Nullable
    R apply(@Nullable D d);

    /** Send a result of the first function to the next one.
     * @param <F> A final result type
     * @param next Next function
     * @return If a result of the first function is {@code null} than the final result is {@code null} too.
     */
    @Nullable
    default <F> Joinable<D, F> add(@NotNull final Joinable<R, F> next) {
        return (D d) -> {
            final R value = apply(d);
            return value != null ? next.apply(value) : null;
        };
    }

    /** Create a joinable function
     *
     * @param <D> Domain value
     * @param <R> Result value
     * @param fce An original function
     * @return The new object type of Function
     */
    @NotNull
    static <D, R> Joinable<D, R> of(@NotNull final Function<D, R> fce) {
        return (D d) -> d != null ? fce.apply(d) : null;
    }
}
