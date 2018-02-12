/*
 * Copyright 2013-2018, Pavel Ponec
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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.gui.MainApplication;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.hotels.service.DbService;
import org.ujorm.hotels.service.SessionService;
import org.ujorm.logger.UjoLoggerFactory;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Common database service implementations
 * @author Pavel Ponec
 */
@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    /** Common DB service */
    @Inject
    private DbService dbService;

    /** Spring application context */
    @Inject
    private ApplicationContext springContext;

    //@Value("${test}") private String test;

    /** Authenticate the user and save the result to the Wicket session */
    @Override
    public boolean authenticate(Customer customer) {
        Customer result = customer==null ? customer
             : dbService.findCustomer
             ( customer.getLogin()
             , customer.getPassword());

        if (result != null) {
            result.lock();
            getSession().saveToSession(result);
            return true;
        } else {
            try {
                Thread.sleep(1500L);
            } catch (InterruptedException e) {
                // Restoring the interrupted status:
                Thread.currentThread().interrupt();
                LOGGER.warn("Interrupted", e);
            }
            return false;
        }
    }

    /** Logout */
    @Override
    public void logout() {
        getSession().logout();
    }

    /** Is logged user ? */
    @Override
    public boolean isLogged() {
        return getSession().isLogged();
    }

    /** Get a login of the current Customer or the {@code null} value */
    @Override
    @Nullable
    public String getLogin() {
        final Customer lc = getSession().getLoggedCustomer();
        return lc != null ? lc.getLogin() : null;
    }

    /** Get a current customer from session or the {@code null} value */
    @Override @Nullable
    public Customer getLoggedCustomer() {
        return getSession().getLoggedCustomer();
    }

    /** Get an immutable logged Customer from session of returns the default Value  */
    @Override @Nonnull
    public Customer getLoggedCustomer(@Nonnull Customer defaultValue) {
        return getSession().getLoggedCustomer(defaultValue);
    }

    /** Is logged admin */
    @Override
    public boolean isAdmin() {
        final Customer lc = getSession().getLoggedCustomer();
        return lc !=null && lc.getAdmin();
    }

    /** Is logged selected user */
    @Override
    public boolean isLogged(Customer customer) {
        final Customer lc =  getSession().getLoggedCustomer();
        return lc != null && lc.getLogin().equals(customer.getLogin());
    }

    /** Get a hash from the text */
    @Override
    public long getHash(@Nullable String text) throws IllegalStateException {
        if (text == null) {
            text = "";
        }
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] digest = md.digest(text.getBytes(UTF_8));
            return new BigInteger(digest).longValue();
        } catch (NoSuchAlgorithmException | RuntimeException | OutOfMemoryError e) {
            throw new IllegalStateException("Method getHash() failed. ", e);
        }
    }

    /** Get session service */
    private SessionService getSession() {
        return springContext.getBean(SessionService.class);
    }

    /** Log environment information */
    @PostConstruct
    protected void init() {
        LOGGER.info(UjoLoggerFactory.getRuntimeInfo(MainApplication.APPLICATION_NAME));
    }
}
