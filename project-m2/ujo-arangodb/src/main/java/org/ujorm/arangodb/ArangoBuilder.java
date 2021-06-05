/*
 * Copyright 2021-2021 Pavel Ponec.
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
 *
 * @author Pavel Ponec
 */
public class ArangoBuilder {

    private final StringBuilder builder = new StringBuilder();
    private final Map<String, Object> params = new HashMap<>();

    public ArangoBuilder() {
    }

    public ArangoBuilder add(String... items) {
        for (String item : items) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(item);
        }
        return this;
    }

    public ArangoBuilder line(String... items) {
        add(items);
        builder.append('\n');
        return this;
    }

    public ArangoBuilder param(Object value) {
        return param(value, getDefaultParameterName());
    }

    public ArangoBuilder param(Object value, String name) {
        params.put(name, value);
        return add("@", name);
    }

    public ArangoBuilder param(OffsetDateTime value) {
        return param(value, getDefaultParameterName());
    }

    public ArangoBuilder param(OffsetDateTime value, String name) {
        return param(value.toEpochSecond(), name);
    }

    public Stream<BaseDocument> query(ArangoDatabase arangoDB) {
        return query(arangoDB, BaseDocument.class);
    }

    public <T> Stream<T> query(ArangoDatabase arangoDB, Class<T> returnType) {
        return arangoDB.query(builder.toString(), params, null, returnType).stream();
    }

    protected String getDefaultParameterName() {
        return "param-" + (params.size() + 1);
    }



}
