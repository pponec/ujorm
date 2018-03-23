/*
 * Copyright 2018, Pavel Ponec
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
package org.ujorm.hotels.service.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.service.SessionService;

/**
 * A scope of the service is a session
 * @author Pavel Ponec
 */
@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionServiceImpl implements SessionService {

    /** Logged user or {@code null} */
    @Nullable
    private Customer loggedUser;

    /** Authenticate the user and save the result to the Wicket session */
    @Override
    public void saveToSession(@Nullable Customer user) {
        loggedUser = user;
    }

   /** Get a current customer from session or the {@code null} value */
    @Nullable
    @Override
    public Customer getLoggedCustomer() {
        return loggedUser;
    }

    /** Get an immutable logged Customer from session of returns the default Value */
    @Override @Nonnull
    public Customer getLoggedCustomer(@Nonnull final Customer defaultValue) {
        final Customer customer = getLoggedCustomer();
        final Customer result = customer != null ? customer : defaultValue;
        Assert.notNull(result, "Default value is required");
        return result;
    }

    /** Logout */
    @Override
    public void logout() {
        loggedUser = null;
    }

    /** Is logged user ? */
    @Override
    public boolean isLogged() {
        return loggedUser != null;
    }
}
