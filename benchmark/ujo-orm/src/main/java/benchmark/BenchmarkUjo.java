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
import org.ujoframework.Ujo;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.core.UjoManager;
import org.ujoframework.criterion.Criterion;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.Session;
import org.ujoframework.orm.metaModel.MetaParams;
import benchmark.bo.*;

/**
 * OrmUjo performance test
 * @author pavel
 */
public class BenchmarkUjo {

    public static final int ORDER_COUNT = 2000;
    public static final int ITEM_COUNT = 7;

    private OrmHandler handler;
    private Session session;


    /** Before the first use you must load a meta-model. */
    public void loadMetaModel() {

        System.out.println("** UJO-ORM " + UjoManager.projectVersion());
        Logger.getLogger(Ujo.class.getPackage().getName()).setLevel(Level.SEVERE);

        long time1 = System.currentTimeMillis();
        boolean yesIWantChangeDefaultParameters = true;
        if (yesIWantChangeDefaultParameters) {
            MetaParams params = new MetaParams();
            MetaParams.TABLE_ALIAS_SUFFIX.setValue(params, "_");
            MetaParams.SEQUENCE_CACHE.setValue(params, 1000);
            //MetaParams.SEQUENCE_CACHE.setValue(params, 1);
            OrmHandler.getInstance().config(params);
        }

        handler = OrmHandler.getInstance();
        handler.loadDatabase(Database.class);
        session = handler.createSession();
        printTime("META-DATA", time1, System.currentTimeMillis());

    }

    /** Create database and using INSERT */
    public void useInsert() {

        long time1 = System.currentTimeMillis();

        PrfUser user1 = new PrfUser();
        user1.set(PrfUser.lastname, "Lorem ipsum dolor");
        user1.set(PrfUser.surename, "Sit amet consectetur");
        user1.set(PrfUser.personalId, "12345678");
        session.save(user1);

        PrfUser user2 = new PrfUser();
        user2.set(PrfUser.lastname, "Lorem ipsum dolor");
        user2.set(PrfUser.surename, "Sit amet consectetur");
        user2.set(PrfUser.personalId, "12345678");
        session.save(user2);


        for (int i=1; i<=ORDER_COUNT; i++) {

            PrfOrder order = new PrfOrder();
            order.set(PrfOrder.dateOfOrder, new Date());
            order.set(PrfOrder.deletionReason, "NO");
            order.set(PrfOrder.discount, new BigDecimal(100));
            order.set(PrfOrder.language, "cs");
            order.set(PrfOrder.orderType, "BX");
            order.set(PrfOrder.paid, true);
            order.set(PrfOrder.parent, null);
            order.set(PrfOrder.paymentType, "C");
            order.set(PrfOrder.publicId, "P"+String.valueOf(1001000+i));
            order.set(PrfOrder.user, user1);
            session.save(order);

            for (int j=1; j<=ITEM_COUNT; j++) {
               PrfOrderItem item = new PrfOrderItem();
               item.set(PrfOrderItem.arrival, false);
               item.set(PrfOrderItem.charge, new BigDecimal(1000-j));
               item.set(PrfOrderItem.description, "Ut diam ante, aliquam ut varius at, fermentum non odio. Aliquam sodales, diam eu faucibus mattis");
               item.set(PrfOrderItem.order, order);
               item.set(PrfOrderItem.price, new BigDecimal(1000+j));
               item.set(PrfOrderItem.publicId, "xxss-"+j);
               item.set(PrfOrderItem.user, user2);
               session.save(item);
            }
        }

        session.commit();
        printTime("INSERT", time1, System.currentTimeMillis());

    }


    /** Create database and using SELECT */
    public void useSelect() {

        long time1 = System.currentTimeMillis();

        Criterion<PrfOrder> crn1 = Criterion.newInstance(PrfOrder.deleted, false);
        UjoIterator<PrfOrder> orders = session.createQuery(crn1).iterate();

        for (PrfOrder order : orders) {
            String surename = order.get(PrfOrder.user).get(PrfUser.surename);
            if (false) System.out.println("Usr.surename: " + surename);

            Criterion<PrfOrderItem> crn2 = Criterion.newInstance(PrfOrderItem.deleted, false);
            Criterion<PrfOrderItem> crn3 = Criterion.newInstance(PrfOrderItem.order, order);
            UjoIterator<PrfOrderItem> items = session.createQuery(crn2.and(crn3)).iterate();

            for (PrfOrderItem item : items) {
                BigDecimal price1 = item.get(PrfOrderItem.price);
                BigDecimal price2 = PrfOrderItem.price.of(item);
                if (true) {
                    String lang = item.get(PrfOrderItem.order).get(PrfOrder.language);
                    String name = item.get(PrfOrderItem.user).get(PrfUser.lastname);
                    if (false) System.out.println(">>> Order.lang: " + lang + " User.lastname" + name);
                }
            }
        }

        session.commit();
        printTime("SELECT", time1, System.currentTimeMillis());

    }


    /** Create database and using DELETE */
    public void useDelete() {

        long time1 = System.currentTimeMillis();

        UjoIterator<PrfOrder> orders = session.createQuery(PrfOrder.class).iterate();
        for (PrfOrder order : orders) {
            Criterion<PrfOrderItem> itemCrn = Criterion.newInstance(PrfOrderItem.order, order);
            session.delete(itemCrn);
            session.delete(order);
        }

        Criterion<PrfUser> userCrn = Criterion.newInstanceTrue(PrfUser.id);
        session.delete(userCrn);

        session.commit();
        printTime("DELETE", time1, System.currentTimeMillis());

    }

    /** Close session */
    @SuppressWarnings("unchecked")
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
            sample.useSelect();
            sample.useDelete();
            sample.useClose();

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
           OrmHandler.getInstance().getSession().close();
        }
    }

}
