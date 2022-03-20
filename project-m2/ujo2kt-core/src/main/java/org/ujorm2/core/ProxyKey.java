/*
 *  Copyright 2020-2022 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.ujorm2.core;

import java.util.function.BiConsumer;
import java.util.function.Function;
import org.ujorm2.Key;

/**
 * Proxy Domain Key of an Object Property
 * @author Pavel Ponec
 */
public class ProxyKey<V> {

    private final Key<?, V> model;

    public ProxyKey(Class clazz, Function<?, V> reader, BiConsumer<?, V> writer) {
        final KeyImpl key = new KeyImpl(clazz);
        key.getPropertyWriter().setReader(reader);
        key.getPropertyWriter().setWriter(writer);
        this.model = key;
    }

    public <T extends Key<?, V>> T get() {
        return (T) model;
    }
}
