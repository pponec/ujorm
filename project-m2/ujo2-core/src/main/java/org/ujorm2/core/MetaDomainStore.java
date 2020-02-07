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
import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;
import org.ujorm2.Key;

/**
 *
 * @author Pavel Ponec
 */
public class MetaDomainStore {

    private final Class<UjoContext> context;

    private final HashMap<Class, AbstractDomainModel> map = new HashMap<>();

    private boolean closed;

    public MetaDomainStore(@Nonnull final Class context) {
        this.context = Assert.notNull(context, "context");
    }

    public <T extends AbstractDomainModel> T newModel(@Nonnull final T key) {
        Assert.validState(!closed, "Factory is locked");
        Assert.notNull(key, "key");

        map.put(key.getDomainClass(), key);
        return key;
    }

    /** Close the domain store - including assigned models */
    public void close() {
        for (Key key : getDomainModels()) {
            if (key instanceof AbstractDomainModel) {
                ((AbstractDomainModel) key).setContext$(null/*context*/);
            }

            if (key instanceof KeyImpl) {
                // set a key context
                ((KeyImpl) key).getPropertyWriter().close();
            }
        }
        closed = true;
    }

    public AbstractDomainModel getDomainModel(@Nonnull final Class domainClass) {
        return map.get(domainClass);
    }

    public Collection<AbstractDomainModel> getDomainModels() {
        return map.values();
    }
}
