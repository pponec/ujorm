/*
 *  Copyright 2020-2020 Pavel Ponec
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;

/**
 *
 * @author Pavel Ponec
 */
public class MetaDomainStore {

    private final Class<UjoContext> context;

    private final HashMap<Class, DomainItem> map = new HashMap<>();

    private final List<DomainItem> list = new ArrayList<>();

    private boolean closed;

    public MetaDomainStore(@Nonnull final Class context) {
        this.context = Assert.notNull(context, "context");
    }

    public <R> DomainItem newModel() {
        final DomainItem result = new DomainItem();
        list.add(result);
        return result;
    }

    /** Close the domain store - including assigned models */
    public void close() {
        if (closed) {
            return;
        }

        getDomainModels().forEach(key -> {
            if (key instanceof AbstractDomainModel) {
                ((AbstractDomainModel) key).setContext$(null/*context*/);
            }

            if (key instanceof KeyImpl) {
                // set a key context
                ((KeyImpl) key).getPropertyWriter().close();
            }
        });

        closed = true;
    }

    public AbstractDomainModel getDomainModel(@Nonnull final Class domainClass) {
        return map.get(domainClass).model();
    }

    public Stream<AbstractDomainModel> getDomainModels() {
        return map.values().stream().map(m -> m.model());
    }


}
