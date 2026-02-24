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
package org.ujorm.mapper;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerService;

/**
 * Singleton to provide Domain handlers.
 * This implementation is thread-safe using the Initialization-on-demand holder idiom.
 */
public final class MapperProvider {

    /** Private constructor to prevent instantiation. */
    private MapperProvider() {
    }

    /**
     * Holder class for lazy-loading the singleton instance.
     */
    private static final class Holder {
        private static final DomainHandlerService INSTANCE = new DomainHandlerService();
    }

    /**
     * Provides the DomainHandlerProvider instance.
     * @return DomainHandlerProvider
     */
    public static DomainHandlerService provider() {
        return Holder.INSTANCE;
    }

    /**
     * Gets a domainService for the specified class.
     * @param clazz An original domain class
     * @return DomainHandler
     */
    public static <D>DomainHandler getHandler(@NotNull Class<D> clazz) {
        return provider().getHandler(clazz);
    }
}