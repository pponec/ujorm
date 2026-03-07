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
package org.ujorm.mapper.service;

import org.jetbrains.annotations.NotNull;
import org.ujorm.Key;
import org.ujorm.mapper.impl.Context;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CommonService {

    /** Find PK. */
    public <D> Key<D,?> findPrimaryKey(Class<D> clazz, Context ctx) {
        var handler = ctx.domainService().getHandler(clazz);
        for (var key : handler.getKeyList()) {
            if (key.primaryKey()) return key;
        }
        if (ctx.config().isFirstPropertyIsIdentifier()) {
            return handler.getKeyList().get(0);
        } else {
            var msg = "No primary key was found by to annotation in " + clazz;
            throw new IllegalStateException(msg);
        }
    }

    /** Compare all values and get result; */
    public <D> Set<String> compareValues(@NotNull D domain1, @NotNull D domain2, @NotNull Context ctx) {
        if (domain1 == null || domain2 == null || ctx == null) {
            throw new IllegalArgumentException("All arguments are required.");
        }
        if (domain1.getClass() != domain2.getClass()) {
            throw new IllegalArgumentException("The data objects must be of the same type.");
        }
        var handler = ctx.domainService().getHandler(domain1.getClass());
        var result = new HashSet<String>(handler.count());
        for (var key : handler.getKeyList()) {
            var keyObject = (Key<D,Object>) key;
            if (!Objects.equals(keyObject.getValue(domain1), keyObject.getValue(domain2))) {
                result.add(keyObject.name());
            }
        }
        return result;
    }

}
