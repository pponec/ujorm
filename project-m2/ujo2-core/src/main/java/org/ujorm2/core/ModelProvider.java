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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 * A store of domain object models
 * @author Pavel Ponec
 */
public class ModelProvider {

    /** Domain - Model Map */
    private final HashMap<Class, MDomain> map = new HashMap<>(8);

    /** A Temporary proxyDomainModels  */
    private List<MDomain> proxyDomainsModels = new ArrayList<>();

    /** An provider of public API */
    private final Tools tools = new Tools();

    public MDomain newModel() {
        final MDomain result = new MDomain();
        proxyDomainsModels.add(result);
        return result;
    }

    /** Close the domain store - including assigned models */
    public void close(@Nonnull final Object motherObject) {
        if (proxyDomainsModels == null) {
            try {
                final List<Field> fields = KeyFactory.getFields(motherObject, proxyDomainsModels);
                for (int i = 0, max = proxyDomainsModels.size(); i < max; i++) {
                    final MDomain proxyDomain = proxyDomainsModels.get(i);
                    final Field field = fields.get(i);
                    final Class modelClass = KeyFactory.getClassFromGenerics(field, true);
                    final AbstractDomainModel abstractDomainModel = (AbstractDomainModel) modelClass.newInstance();
                    abstractDomainModel.getDirecKey().setContext(this);
                    map.put(modelClass, proxyDomain);
                    proxyDomain.close(abstractDomainModel);
                }
            } catch (SecurityException | ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            } finally {
                // Close the object:
                proxyDomainsModels = null;
            }
        }
    }

    public AbstractDomainModel getDomainModel(@Nonnull final Class domainClass) {
        return map.get(domainClass).get();
    }

    public Stream<AbstractDomainModel> getDomainModels() {
        return map.values().stream().map(m -> m.get());
    }

    /** Provides an extended API */
    public final Tools getTools() {
        return tools;
    }

    // --- Inner class()  ---

    public final class Tools {

        private Tools() {
        }

    }

}
