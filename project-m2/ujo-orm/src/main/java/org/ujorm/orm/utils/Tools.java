/*
 * Copyright 2026-2026 Pavel Ponec
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
package org.ujorm.orm.utils;

import org.jetbrains.annotations.NotNull;
import org.ujorm.DomainHandler;

import java.util.Arrays;
import java.util.Objects;

public class Tools {

    /**
     * Splits an array into batches using Object[][] for a concise implementation
     * without reflection. Assumes batchSize is always greater than zero.
     *
     * @param batchSize The maximum size of each resulting batch.
     * @param ids       The original array to be partitioned.
     * @param <V>       The generic type of the input array elements.
     * @return A 2D Object array containing the individual batches.
     */
    @SafeVarargs
    public static <V> Object[][] splitIntoBatches(int batchSize, @NotNull V... ids) {
        if (ids.length <= batchSize) {
            return new Object[][]{ids};
        }

        var chunksCount = (ids.length + batchSize - 1) / batchSize;
        var result = new Object[chunksCount][];

        for (var i = 0; i < chunksCount; i++) {
            var start = i * batchSize;
            var end = Math.min(start + batchSize, ids.length);
            result[i] = Arrays.copyOfRange(ids, start, end);
        }
        return result;
    }

    /** Build a property change set */
    @NotNull
    public static <D> BitSet findChanges(@NotNull D domain, @NotNull D snapshot, @NotNull DomainHandler<D> handler) {
        if (domain == null || snapshot == null) {
            var msg = "The %s object type of %s is required".formatted(
                    domain == null ? "domain" : "snapshot",
                    handler.getDomainClass().getSimpleName());
            throw new IllegalArgumentException(msg);
        }

        var keys = handler.getKeyList();
        var result = BitSet.of(keys.size());
        for (var key : handler.getKeyList()) {
            var v1 = key.getValue(domain);
            var v2 = key.getValue(snapshot);
            if (!Objects.equals(v1, v2)) {
                result.setValue(key.index(), true);
            }
        }
        return result;
    }
}
