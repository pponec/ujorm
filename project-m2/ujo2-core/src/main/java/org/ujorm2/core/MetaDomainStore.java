/*
 *  Copyright 2011-2014 Pavel Ponec
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

import java.util.Collection;
import java.util.HashMap;
import org.ujorm.tools.Assert;
import org.ujorm2.Key;

/**
 *
 * @author Pavel Ponec
 */
public class MetaDomainStore {

    private final HashMap<Class, Key> map = new HashMap<>();

    private boolean closed;

    public <T extends KeyImpl> T addModel(T key) {
        Assert.validState(!closed, "Factory is locked");
        map.put(key.getDomainClass(), key);
        return key;
    }

    public void close() {
        for (Key key : getKeys()){
            if (key instanceof KeyImpl) {
                // set a key context
                ((KeyImpl) key).getPropertyWriter().close();
            }
        }
        closed = true;
    }

    public <T extends Key> T getKey(Class clazz) {
        return (T) map.get(clazz);
    }

    public Collection<Key> getKeys() {
        return map.values();
    }

}
