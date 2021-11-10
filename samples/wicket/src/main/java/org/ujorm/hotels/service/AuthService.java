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
 * Common database service
 * @author Pavel Ponec
 */
public interface AuthService {

    /** Authenticate the user and save the result to the Wicket session */
    public boolean authenticate(Customer customer);

    /** Logout */
    public void logout();

    /** Is logged any user ? */
    public boolean isLogged();

    /** Is logged an administrator ? */
    public boolean isAdmin();

    /** Is logged selected user */
    public boolean isLogged(Customer customer);

    /** Get a login of the current Customer or the {@code null} value */
    @Nullable
    public String getLogin();

    /** Get an immutable logged customer from session */
    @Nullable
    public Customer getLoggedCustomer();

    /** Get an immutable logged customer from session or returns a default value  */
    @NotNull
    public Customer getLoggedCustomer(@NotNull Customer defaultValue);

}
