/*
 * Copyright 2013 ponec.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.services.*;
import org.ujorm.validator.ValidationException;
import static org.ujorm.core.UjoManager.*;
/**
 * Common database service implementations
 * @author ponec
 */
@Transactional
public class DbServiceImpl extends AbstractServiceImpl implements DbService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbServiceImpl.class);

    @Autowired
    private AuthService authService;

    /** Read only sign */
    private boolean readOnly;

    /** Read only sign */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /** Load Customer by login using a transaction. */
    @Override
    public Customer getCustomer(String login) {
        return super.createQuery(Customer.LOGIN.whereEq(login)).uniqueResult();
    }

    /** {@inheritDoc } */
    @Override
    public void deleteHotel(Hotel hotel) {
        LOGGER.info("Delete hotel {}", hotel);
        checkReadOnly();
        getSession().delete(hotel);
    }

    /** {@inheritDoc } */
    @Override
    public void updateHotel(Hotel hotel) {
        LOGGER.info("Update hotel {}", hotel);
        checkReadOnly();
        getSession().update(hotel);
    }

    @Override
    public void deleteCustomer(Customer customer) {
        LOGGER.info("Delete customer {}", customer);
        checkReadOnly();
        getSession().delete(customer);
    }

    /** Update customer */
    @Override
    public void updateCustomer(Customer customer) {
        LOGGER.info("Update customer {}", customer);
        checkReadOnly();

        String password = customer.get(Customer.PASSWORD);
        if (isFilled(password)) {
            customer.writeSession(getSession()); // Activate modifications
            customer.set(Customer.PASSWORD_HASH, authService.getHash(password));
        }

        getSession().update(customer);
    }

    /** Check a read-only state */
    private void checkReadOnly() throws ValidationException {
        if (readOnly) {
            throw new ValidationException("exception.readOnly"
                , "It is allowed a read only actions, download the project for all features.");
        }
    }

}
