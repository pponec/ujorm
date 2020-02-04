package org.ujorm2.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.ujorm2.doman.Item;
import org.ujorm2.doman.Order;
import org.ujorm2.doman.Order.State;
import org.ujorm2.doman.User;

/**
 *
 * @author Pavel Ponec
 */
public class HelpService {

    private final LocalDateTime timeNow = LocalDateTime.parse("2020-02-04T16:07");

    public LocalDateTime now() {
        return timeNow;
    }

    public Iterable<Item> findItemsService() {
        final List<Item> result = new ArrayList<>();
        result.add(createItem(1, "100"));
        result.add(createItem(2, "200"));
        result.add(createItem(3, "300"));
        result.add(createItem(4, "400"));
        result.add(createItem(5, "500"));

        return result;
    }

    public Item createItem(Integer id, String price) {
        final Item result = new Item();
        result.setId(id);
        result.setNote("Test" + id);
        result.setPrice(new BigDecimal(price));
        result.setOrder(createOrder());

        return result;
    }

    public Order createOrder() {
        final Order result = new Order();
        result.setId(1);
        result.setCreated(now());
        result.setState(State.ACTIVE);
        result.setTotalPrice(BigDecimal.TEN);
        result.setUser(createUser());
        return result;
    }

    public User createUser() {
        final User user = new User();
        user.setId(1);
        user.setPin((short) 1234);
        user.setFirstName("Joe");
        user.setSureName("Black");
        user.setBorn(timeNow.minusYears(40));
        user.setParent(null);

        return user;
    }

}
