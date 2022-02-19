/*
 *  Copyright 2013-2022 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.transaction.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ujorm.criterion.Criterion;
import org.ujorm.spring.UjormTransactionManager;
import org.ujorm.transaction.domains.Order;
import org.ujorm.transaction.service.OrderService;

/** An Order service implementation */
@Service("orderService")
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private UjormTransactionManager manager;

    /** Uloží ménu */
    @Transactional
    @Override
    public void save(Order order) {
        manager.getLocalSession().insert(order);
    }

    /** Aktualizuje měnu */
    @Transactional
    @Override
    public void update(Order order) {
        manager.getLocalSession().update(order);
    }

    /** Smaže měnu */
    @Transactional
    @Override
    public void delete(Order order) {
        manager.getLocalSession().delete(order);
    }    

    /** Načte měnu podle ID */
    @Transactional
    @Override
    public Order getOrder(Long orderId) {
        final Criterion<Order> criterion = Order.ID.whereEq(orderId);
        return manager.getLocalSession().createQuery(criterion).uniqueResult();
    }
}
