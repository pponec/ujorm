/*
 *  Copyright 2009 Paul Ponec
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

package benchmark;

import java.math.BigDecimal;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujorm.Ujo;
import org.ujorm.core.UjoIterator;
import org.ujorm.core.UjoManager;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.Session;
import org.ujorm.orm.metaModel.MetaParams;
import benchmark.bo.*;
import org.ujorm.orm.Transaction;

/**
 * OrmUjo performance test
 * @author Pavel Ponec
 */
public class BenchmarkUjo {

    public static final int ORDER_COUNT = 2000;
    public static final int ITEM_COUNT = 7;

    private OrmHandler handler;
    private Session session;


    /** Before the first use you must load a meta-model. */
    public void loadMetaModel() {

        System.out.println("** Ujorm " + UjoManager.projectVersion());
        Logger.getLogger(Ujo.class.getPackage().getName()).setLevel(Level.SEVERE);

        long time1 = System.currentTimeMillis();
        boolean yesIWantChangeDefaultParameters = true;
        handler = new OrmHandler();
        if (yesIWantChangeDefaultParameters) {
            MetaParams params = new MetaParams();
            MetaParams.TABLE_ALIAS_SUFFIX.setValue(params, "_");
            MetaParams.SEQUENCE_CACHE.setValue(params, 1000);
            //MetaParams.SEQUENCE_CACHE.setValue(params, 1);
            handler.config(params);
        }

        handler.loadDatabase(Database.class);
        session = handler.createSession();
        printTime("META-DATA", time1, System.currentTimeMillis());

    }

    /** Create database and using INSERT */
    public void useInsert() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        UjoUser user1 = new UjoUser();
        user1.set(UjoUser.LASTNAME, "Lorem ipsum dolor");
        user1.set(UjoUser.SURENAME, "Sit amet consectetur");
        user1.set(UjoUser.PERSONAL_ID, "12345678");
        session.save(user1);

        UjoUser user2 = new UjoUser();
        user2.set(UjoUser.LASTNAME, "Lorem ipsum dolor");
        user2.set(UjoUser.SURENAME, "Sit amet consectetur");
        user2.set(UjoUser.PERSONAL_ID, "12345678");
        session.save(user2);

        for (int i=1; i<=ORDER_COUNT; i++) {
            UjoOrder order = new UjoOrder();
            order.set(UjoOrder.DATE_OF_ORDER, new Date());
            order.set(UjoOrder.DELETION_REASON, "NO");
            order.set(UjoOrder.DISCOUNT, new BigDecimal(100));
            order.set(UjoOrder.LANGUAGE, "cs");
            order.set(UjoOrder.ORDER_TYPE, "BX");
            order.set(UjoOrder.PAID, true);
            order.set(UjoOrder.PARENT, null);
            order.set(UjoOrder.PAYMENT_TYPE, "C");
            order.set(UjoOrder.PUBLIC_ID, "P"+String.valueOf(1001000+i));
            order.set(UjoOrder.USER, user1);
            session.save(order);

            for (int j=1; j<=ITEM_COUNT; j++) {
               UjoOrderItem item = new UjoOrderItem();
               item.set(UjoOrderItem.ARRIVAL, false);
               item.set(UjoOrderItem.CHARGE, new BigDecimal(1000-j));
               item.set(UjoOrderItem.DESCRIPTION, "Ut diam ante, aliquam ut varius at, fermentum non odio. Aliquam sodales, diam eu faucibus mattis");
               item.set(UjoOrderItem.ORDER, order);
               item.set(UjoOrderItem.PRICE, new BigDecimal(1000+j));
               item.set(UjoOrderItem.PUBLIC_ID, "xxss-"+j);
               item.set(UjoOrderItem.USER, user2);
               session.save(item);
            }
        }

        tr.commit();
        printTime("INSERT", time1, System.currentTimeMillis());
    }


    /** Test the single SELECT */
    public void useSingleSelect() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        final Criterion<UjoOrderItem> crn1, crn2;
        crn1 = UjoOrderItem.DELETED.whereEq(false);
        crn2 = UjoOrderItem._ORDER_DELETED.whereEq(false);

        UjoIterator<UjoOrderItem> items = session.createQuery(crn1.and(crn2)).iterator();

        int i = 0;
        for (UjoOrderItem item : items) {
            ++i;
            Long id = item.get(UjoOrderItem.ID);
            BigDecimal price = item.get(UjoOrderItem.PRICE);
            if (false) {
                System.out.println(">>> Item.id: " + id + " " + price);
            }
        }

        tr.commit();
        printTime("SINGLE SELECT "+i, time1, System.currentTimeMillis());
    }

    /** Test the EMPTY SELECT */
    public void useEmptySelect() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        for (int i = -ORDER_COUNT; i<0 ; i++) {
            final Criterion<UjoOrder> crn1, crn2;
            crn1 = UjoOrder.ID.whereEq(new Long(i));
            crn2 = UjoOrder.DELETED.whereEq(true);

            UjoIterator<UjoOrder> orders = session.createQuery(crn1.and(crn2)).iterator();
            orders.hasNext();
        }
        tr.commit();
        printTime("EMPTY SELECT "+ORDER_COUNT, time1, System.currentTimeMillis());
    }

    /** Test the multi SELECT */
    public void useMultiSelect() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        final Criterion<UjoOrder> crn1 = UjoOrder.DELETED.whereEq(false);
        UjoIterator<UjoOrder> orders = session.createQuery(crn1).iterator();

        int i = 0;
        for (UjoOrder order : orders) {
            String surename = order.get(UjoOrder.USER).get(UjoUser.SURENAME);
            if (false) System.out.println("Usr.surename: " + surename);

            final Criterion<UjoOrderItem> crn2,  crn3;
            crn2 = UjoOrderItem.DELETED.whereEq(false);
            crn3 = UjoOrderItem.ORDER.whereEq(order);

            UjoIterator<UjoOrderItem> items = session.createQuery(crn2.and(crn3)).iterate();

            for (UjoOrderItem item : items) {
                ++i;
                BigDecimal price = item.get(UjoOrderItem.PRICE);
                BigDecimal charge = item.get(UjoOrderItem.CHARGE);
                if (true) {
                    String lang = item.get(UjoOrderItem.ORDER).get(UjoOrder.LANGUAGE);
                    String name = item.get(UjoOrderItem.USER).get(UjoUser.LASTNAME);
                    if (false) System.out.println(">>> Order.lang: " + lang + " User.lastname" + name);
                }
            }
        }

        tr.commit();
        printTime("MULTI SELECT "+i, time1, System.currentTimeMillis());

    }

    /** Update a charge of the order items */
    public void useUpdate() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        final Criterion<UjoOrderItem> crn1, crn2;
        crn1 = UjoOrderItem.DELETED.whereEq(false);
        crn2 = UjoOrderItem._ORDER_DELETED.whereEq(false);

        UjoIterator<UjoOrderItem> items = session.createQuery(crn1.and(crn2)).iterate();

        int i = 0;
        for (UjoOrderItem item : items) {
            ++i;
            item.set(UjoOrderItem.CHARGE, item.get(UjoOrderItem.CHARGE).add(BigDecimal.ONE));
            session.update(item);
        }

        tr.commit();
        printTime("UPDATE "+i, time1, System.currentTimeMillis());
    }

    /** How to use DELETE */
    public void useDelete() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        UjoIterator<UjoOrder> orders = session.createQuery(UjoOrder.class).iterate();
        for (UjoOrder order : orders) {
            session.delete(UjoOrderItem.ORDER.whereEq(order));
            session.delete(order);
        }
        session.delete(UjoUser.ID.forAll());

        tr.commit();
        printTime("DELETE", time1, System.currentTimeMillis());

    }

    /** Close session */
    public void useClose() {
        session.close();
    }


    /** Print time message. */
    protected void printTime(String msg, long time1, long time2) {
        long time = time2-time1;
        double result = time/1000d;
        System.out.println("TIME - "+msg + ": "+result);
    }


    /** Test */
    public static void main(String[] args) {
        try {
            BenchmarkUjo sample = new BenchmarkUjo();

            sample.loadMetaModel();
            sample.useInsert();
            sample.useSingleSelect();
            sample.useEmptySelect();
            sample.useMultiSelect();
            sample.useUpdate();
            sample.useDelete();
            sample.useClose();

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
           OrmHandler.getInstance().getSession().close();
        }
    }

}
