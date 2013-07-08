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

import java.math.BigInteger;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.core.UjoService;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.entity.Hotel;
import org.ujorm.hotels.services.*;

/**
 * Common database service implementations
 * @author ponec
 */
@Service
@Transactional
public class DbServiceImpl extends AbstractServiceImpl implements DbService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DbServiceImpl.class);

    /** Load Customer by login using a transaction. */
    @Override
    public Customer getCustomer(String login) {
        return super.createQuery(Customer.LOGIN.whereEq(login)).uniqueResult();
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

    /** {@inheritDoc } */
    @Override
    public void deleteHotel(Hotel hotel) {
        LOGGER.info("Delete hotel {}", hotel);
        super.getSession().delete(hotel);
    }

    /** {@inheritDoc } */
    @Override
    public void updateHotel(Hotel hotel) {
        LOGGER.info("Update hotel {}", hotel);
        super.getSession().update(hotel);
    }

}
