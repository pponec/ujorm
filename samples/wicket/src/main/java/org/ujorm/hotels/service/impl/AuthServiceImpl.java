/*
 * Copyright 2013-2015, Pavel Ponec
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
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ujorm.core.UjoService;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.gui.MainApplication;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.hotels.service.DbService;
import org.ujorm.logger.UjoLoggerFactory;
/**
 * Common database service implementations
 * @author Pavel Ponec
 */
@Service
public class AuthServiceImpl extends AbstractServiceImpl<Customer> implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceImpl.class);

    /** Session attribute name */
    private static final String CUSTOMER_ATTR = "CUSTOMER_ATTR";

    /** Common DB service */
    @Autowired private DbService dbService;

    /** Authenticate the user and save the result to the Wicket session */
    @Override
    public boolean authenticate(Customer customer) {
        Customer result = customer==null ? customer
             : dbService.findCustomer
             ( customer.getLogin()
             , customer.getPassword());

        if (result != null) {
            result.lock();
            getThreadSession().setAttribute(CUSTOMER_ATTR, result);
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
        getThreadSession().setAttribute(CUSTOMER_ATTR, null);
        // session.invalidate(); // restoring tabs
    }

    /** Is logged user ? */
    @Override
    public boolean isLogged() {
        return getLoggedCustomer() != null;
    }

    /** Get a login of the current Customer or the {@code null} value */
    @Override
    @Nullable
    public String getLogin() {
        final Customer lc = getLoggedCustomer();
        return lc != null ? lc.getLogin() : null;
    }

    /** Get a current customer from session or the {@code null} value */
    @Nullable
    @Override
    public Customer getLoggedCustomer() {
        final Session session = getThreadSession();
        final Object result = session != null
                ? session.getAttribute(CUSTOMER_ATTR) : null;
        return (Customer) result;
    }

    /** Get an immutable logged Customer from session of returns the default Value  */
    @Override
    public Customer getLoggedCustomer(Customer defaultValue) {
        final Customer result = getLoggedCustomer();
        return result != null ? result : defaultValue;
    }

    /** Is logged admin */
    @Override
    public boolean isAdmin() {
        final Customer lc = getLoggedCustomer();
        return lc !=null && lc.getAdmin();
    }

    /** Is logged selected user */
    @Override
    public boolean isLogged(Customer customer) {
        final Customer lc = getLoggedCustomer();
        return lc != null && lc.getLogin().equals(customer.getLogin());
    }

    /** Get a hash from the text */
    @Override
    public long getHash(String text) throws IllegalStateException {
        if (text == null) {
            text = "";
        }
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] digest = md.digest(text.getBytes(UjoService.UTF_8));
            return new BigInteger(digest).longValue();
        } catch (Throwable e) {
            throw new IllegalStateException("Method getHash() failed. ", e);
        }
    }

    /** Return a Session or {@code null} if no session was found. */
    private Session getThreadSession() {
        return ThreadContext.getSession();
    }

    /** Log environment information */
    @PostConstruct
    protected void init() {
        LOGGER.info(UjoLoggerFactory.getRuntimeInfo(MainApplication.APPLICATION_NAME));
    }
}
