package org.ujorm.service;

import java.time.LocalDateTime;
import java.util.List;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.doman.Item;
import org.ujorm.doman.Order;
import org.ujorm.doman.User;
import org.ujorm.metamodel.MetaItem;
import org.ujorm.metamodel.MetaOrder;

/**
 *
 * @author Pavel Ponec
 */
public class MySampleService {

    /** Reading / writing */
    public void doOrderAccess() {
        MetaOrder<Order> metaOrder = MetaOrder.of();

        Key<Order, Integer> orderIdKey = metaOrder.id();
        Key<Order, String> userNameKey = metaOrder.user().firstName();

        Order order = metaOrder.createDomain();
        orderIdKey.setValue(1, order);
        userNameKey.setValue("Pavel", order);
        Integer id = orderIdKey.getValue(order);
        String name = userNameKey.getValue(order);
    }

    /** Reading / writing */
    public void doItemAccess() {
        MetaItem<Item> metaItem = MetaItem.of();

        Key<Item, Integer> itemIdKey = metaItem.id();
        Key<Item, User> userKey = metaItem.order().user();
        Key<Item, Short> pinKey = metaItem.order().user().pin();

        Item item = metaItem.createDomain();
        itemIdKey.setValue(1, item);
        Integer orderId1 = itemIdKey.getValue(item);
        userKey.setValue(new User(), item);
        User user = userKey.getValue(item);
        pinKey.setValue((short) 125, item);
        Short userPin = pinKey.getValue(item);
    }

    /** Criterions */
    public void doItemCondition() {
        MetaItem<Item> mItem = MetaItem.of();

        Criterion<Item> itemCrn1 = mItem.forAll();
        List<Item> items = itemCrn1.select(findItemsService());

        Criterion<Item> crn1 = mItem.order().id().forEq(10);
        Criterion<Item> crn2 = mItem.order().created().forLe(LocalDateTime.now());
        Criterion<Item> crn3 = crn1.and(crn2);
        List<Item> result = crn3.select(findItemsService());
    }

    private Iterable<Item> findItemsService() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
