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
package org.ujorm.orm.service;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.orm.impl.Context;
import org.ujorm.orm.utils.BitSet;

import java.util.Objects;

public class CommonService {

    /** Find PK. */
    public <D> Key<D,?> findPrimaryKey(Class<D> clazz, Context ctx) {
        var handler = ctx.domainService().getHandler(clazz);
        for (var key : handler.getKeyList()) {
            if (key.info().primaryKey()) return key;
        }
        if (ctx.config().isFirstPropertyIsIdentifier()) {
            return handler.getKeyList().get(0);
        } else {
            var msg = "No primary key was found by to annotation in " + clazz;
            throw new IllegalStateException(msg);
        }
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
