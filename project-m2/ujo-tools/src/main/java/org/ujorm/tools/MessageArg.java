/*
 * Copyright 2012-2017 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools;

import java.io.Serializable;
import java.util.Map;

/**
 * Message Argument
 * @author Pavel Ponec
 * @see MessageService
 * @since 1.54
 */
public final class MessageArg<T> implements Serializable {

    /** Key name */
    private final String name;

    public MessageArg(String name) {
        this.name = name;
    }

    /** Returns the name */
    @Override
    public String toString() {
        return name;
    }

    /** Get a value from a map */
    public T getValue(final Map<String, Object> map) {
        return (T) map.get(name);
    }
}
