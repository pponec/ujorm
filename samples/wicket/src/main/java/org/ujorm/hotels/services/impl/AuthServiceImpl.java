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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.core.UjoService;
import org.ujorm.criterion.Criterion;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.services.*;
/**
 * Common database service implementations
 * @author ponec
 */
@Service
public class AuthServiceImpl extends AbstractServiceImpl implements AuthService {

    private static final String CUSTOMER_ATTR = "CUSTOMER_ATTR";

    /** Authenticate the user */
    @Override
    public boolean authenticate(Customer customer, Session session) {
        return authenticate
             ( customer.get(Customer.LOGIN)
             , customer.get(Customer.PASSWORD)
             , session );
    }


    /** Authenticate the user */
    @Transactional
    @Override
    public boolean authenticate(String login, String password, Session session) {
        final Criterion<Customer> crn1, crn2, crn3, crn4;
        crn1 = Customer.LOGIN.whereEq(login);
        crn2 = Customer.PASSWORD_HASH.whereEq(getHash(password));
        crn3 = Customer.ACTIVE.whereEq(true);
        crn4 = crn1.and(crn2).and(crn3);

        Customer customer = getSession().createQuery(crn4).uniqueResult();
        if (customer != null) {
            customer.writeSession(null);
            session.setAttribute(CUSTOMER_ATTR, customer);
            return true;
        } else {
            return false;
        }
    }

    /** Logout */
    @Override
    public void logout(Session session) {
        session.invalidate();
    }

    /** Is logged user ? */
    @Override
    public boolean isCustomer(Session session) {
        return getCurrentCustomer(session) != null;
    }

    /** Get current customer from session  */
    @Override
    public Customer getCurrentCustomer(Session session) {
        Object result = session.getAttribute(CUSTOMER_ATTR);
        return result instanceof Customer
                ? (Customer) result
                : null ;
    }



    /** Is logged admin */
    @Override
    public boolean isAdmin(Session session) {
        final Customer customer = (Customer) session.getAttribute(CUSTOMER_ATTR);
        return customer !=null && customer.get(Customer.ADMIN);
    }


    /** Get a hash from the text */
    @Override
    public long getHash(String text) throws IllegalStateException {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] digest = md.digest(text.getBytes(UjoService.UTF_8));
            return new BigInteger(digest).longValue();
        } catch (Throwable e) {
            throw new IllegalStateException("Method getHash() failed. ", e);
        }
    }

}
