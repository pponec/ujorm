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
package org.ujorm.hotels.services.impl;

import java.math.BigInteger;
import java.security.MessageDigest;
import org.apache.wicket.Session;
import org.apache.wicket.ThreadContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.ujorm.core.UjoService;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.services.*;
import org.ujorm.validator.ValidationException;
/**
 * Common database service implementations
 * @author Pavel Ponec
 */
public class AuthServiceImpl extends AbstractServiceImpl implements AuthService {

    private static final String CUSTOMER_ATTR = "CUSTOMER_ATTR";

    @Autowired
    private DbService dbService;

    /** Authenticate the user and save the result to the Wicket session */
    @Override
    public boolean authenticate(Customer customer) {
        Customer result = customer==null ? customer : dbService.findCustomer
             ( customer.get(Customer.LOGIN)
             , customer.get(Customer.PASSWORD));

        if (result != null) {
            result.writeSession(null);
            getThreadSession().setAttribute(CUSTOMER_ATTR, result);
            return true;
        } else {
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
    public boolean isCustomer() {
        return getCurrentCustomer() != null;
    }

    /** Get current customer from session  */
    @Override
    public Customer getCurrentCustomer() {
        Session session = getThreadSession();
        Object result = session != null
                ? session.getAttribute(CUSTOMER_ATTR) : null;
        return result instanceof Customer
                ? (Customer) result
                : null ;
    }

    /** Get current customer from session of returns the default Value  */
    @Override
    public Customer getCurrentCustomer(Customer defaultValue) {
        Customer result = getCurrentCustomer();
        return result != null ? result : defaultValue;
    }

    /** Is logged admin */
    @Override
    public boolean isAdmin() {
        final Customer customer = (Customer) getThreadSession().getAttribute(CUSTOMER_ATTR);
        return customer !=null && customer.get(Customer.ADMIN);
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

}
