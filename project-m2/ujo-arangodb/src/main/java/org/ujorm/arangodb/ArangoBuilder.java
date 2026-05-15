/*
 * Copyright 2021-2026 Pavel Ponec.
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
package org.ujorm.arangodb;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;

/**
 * A prototype of the ArangoDB builder.
 *
 * See: https://www.arangodb.com/tutorials/tutorial-java-driver/
 *
 * @author Pavel Ponec
 */
public class ArangoBuilder {

    /** Query builder */
    private final StringBuilder builder = new StringBuilder();
    /** Query parameters */
    private final Map<String, Object> params = new HashMap<>();

    /** Add items to the builder */
    public ArangoBuilder add(String... items) {
        for (var item : items) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(item);
        }
        return this;
    }

    /** Add items and a new line */
    public ArangoBuilder line(String... items) {
        add(items);
        builder.append('\n');
        return this;
    }

    /** Add a parameter with a default name */
    public ArangoBuilder param(Object value) {
        return param(value, getDefaultParameterName());
    }

    /** Add a parameter with a specific name */
    public ArangoBuilder param(Object value, String name) {
        params.put(name, value);
        builder.append(" @").append(name);
        return this;
    }

    /** Add a date parameter with a default name */
    public ArangoBuilder param(OffsetDateTime value) {
        return param(value, getDefaultParameterName());
    }

    /** Add a date parameter with a specific name */
    public ArangoBuilder param(OffsetDateTime value, String name) {
        return param(value.toEpochSecond(), name);
    }

    /** Execute the query */
    public Stream<BaseDocument> execute(ArangoDatabase arangoDB) {
        return execute(arangoDB, BaseDocument.class);
    }

    /** Execute the query */
    public <T> Stream<T> execute(ArangoDatabase arangoDB, Class<T> returnType) {
        return arangoDB.query(builder.toString(), params, null, returnType).stream();
    }

    /** Get a default parameter name */
    protected String getDefaultParameterName() {
        return "param" + (params.size() + 1);
    }

    /** AQL statement */
    @Override
    public String toString() {
        return builder.toString();
    }
}