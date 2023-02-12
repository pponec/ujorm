/*
 * Copyright 2013, Pavel Ponec
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
package org.ujorm.hotels.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.hotels.entity.Customer;

/**
 * Common session service
 * @author Pavel Ponec
 */
public interface SessionService {

    /** Authenticate the user and save the result to the Wicket session */
    void saveToSession(@Nullable Customer user);

   /** Get a current customer from session or the {@code null} value */
    @Nullable Customer getLoggedCustomer();

    /** Get an immutable logged Customer from session of returns the default Value
     * @return  */
    @NotNull Customer getLoggedCustomer(@NotNull Customer defaultValue);

    /** Logout */
    void logout();

    /** Is logged user ? */
    boolean isLogged();

}
