package org.ujorm.service;

import org.ujorm.Key;
import org.ujorm.doman.Item;
import org.ujorm.doman.Order;
import org.ujorm.doman.User;
import org.ujorm.metamodel.MetaItem;
import org.ujorm.metamodel.MetaOrder;

/**
 *
 * @author Pavel Ponec
 */
public class MyService {

    public void doSomethingWithOrder() {

        MetaOrder<Order> mOrder = new MetaOrder<>();
        Key<Order, Integer> orderIdKey = mOrder.id();
        Key<Order, String> orderUserNameKey = mOrder.user().firstName();

        Order order = new Order();

        orderIdKey.set(order, 1);
        orderUserNameKey.set(order, "Pavel");

        Integer id = orderIdKey.get(order);
        String name = orderUserNameKey.get(order);
    }

    public void doSomethingWithItem() {

        MetaItem<Item> mItem = new MetaItem<>();
        Key<Item, Integer> itemIdKey = mItem.id();
        Key<Item, User> itemUserKey = mItem.order().user();
        Key<Item, Short> itemUserPinKey = mItem.order().user().pin();

        Item item = new Item();

        itemIdKey.set(item, 1);
        Integer orderId1 = itemIdKey.get(item);

        itemUserKey.set(item, new User());
        User user = itemUserKey.get(item);

        itemUserPinKey.set(item, (short) 125);
        Short pin = itemUserPinKey.get(item);
    }

}
