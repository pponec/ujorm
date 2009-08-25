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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import benchmark.bo.*;

/**
 * OrmUjo performance test
 * @author pavel
 */
public class BenchmarkHP {

    public static final int ORDER_COUNT = 2000;
    public static final int ITEM_COUNT  = 7;

    SessionFactory sessionFactory;
    Session session;

    /** Before the first use you must load a meta-model. */
    public void loadMetaModel() {

        System.out.println("** HIBERNATE " + "3.3.1.GA");
        Logger.getLogger("").setLevel(Level.SEVERE);

        long time1 = System.currentTimeMillis();

        sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
        session = sessionFactory.openSession();

        printTime("META-DATA", time1, System.currentTimeMillis());
    }

    /** Create database and using INSERT */
    public void useInsert() {

        long time1 = System.currentTimeMillis();

        Transaction tr = session.beginTransaction();

        PrfUser user1 = new PrfUser();
        user1.setLastname("Lorem ipsum dolor");
        user1.setSurename( "Sit amet consectetur");
        user1.setPersonalId( "12345678");
        session.save(user1);

        PrfUser user2 = new PrfUser();
        user2.setLastname( "Lorem ipsum dolor");
        user2.setSurename( "Sit amet consectetur");
        user2.setPersonalId( "12345678");
        session.save(user2);

        for (int i=1; i<=ORDER_COUNT; i++) {

            PrfOrder order = new PrfOrder();
            order.setDateOfOrder( new Date());
            order.setDeletionReason("NO");
            order.setDiscount(new BigDecimal(100));
            order.setLanguage("cs");
            order.setOrderType("BX");
            order.setPaid(true);
            order.setParent(null);
            order.setPaymentType("C");
            order.setPublicId( "P"+String.valueOf(1001000+i));
            order.setUser(user1);
            session.save(order);

            for (int j=1; j<=ITEM_COUNT; j++) {
               PrfOrderItem item = new PrfOrderItem();
               item.setArrival(false);
               item.setCharge( new BigDecimal(1000-j));
               item.setDescription( "Ut diam ante, aliquam ut varius at, fermentum non odio. Aliquam sodales, diam eu faucibus mattis");
               item.setOrder( order);
               item.setPrice( new BigDecimal(1000+j));
               item.setPublicId( "xxss-"+j);
               item.setUser( user2);
               session.save(item);
            }
        }

        tr.commit();
        printTime("INSERT", time1, System.currentTimeMillis());

    }


    /** Create database and using SELECT */
    @SuppressWarnings("unchecked")
    public void useSingleSelect() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        String hql = "from PrfOrderItem where deleted = :deleted and order.deleted = :deleted";
        Query query = session.createQuery(hql);
        query.setParameter("deleted", false);
        List<PrfOrderItem> items = (List<PrfOrderItem>) query.list();

        int i = 0;
        for (PrfOrderItem item : items) {
            ++i;
            Long id = item.getId();
            BigDecimal price = item.getPrice();
            if (false) {
                System.out.println(">>> Item.id: " + id + " " + price);
            }
        }

        tr.commit();
        printTime("SINGLE SELECT "+i, time1, System.currentTimeMillis());
    }


    /** Create database and using SELECT */
    @SuppressWarnings("unchecked")
    public void useEmptySelect() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();


        for (int i = -ORDER_COUNT; i<0 ; i++) {
            String hql = "from PrfOrder where id = :id and deleted = :deleted";
            Query query = session.createQuery(hql);
            query.setParameter("id", new Long(i));
            query.setParameter("deleted", true);
            List<PrfOrder> items = (List<PrfOrder>) query.list();
        }

        tr.commit();
        printTime("EMPTY SELECT "+ORDER_COUNT, time1, System.currentTimeMillis());;
    }


    /** Create database and using SELECT */
    @SuppressWarnings("unchecked")
    public void useMultiSelect() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        String hql = "from PrfOrder where deleted = :deleted";
        Query query = session.createQuery(hql);
        query.setParameter("deleted", false);
        List<PrfOrder> orders = (List<PrfOrder>) query.list();

        int i = 0;
        for (PrfOrder order : orders) {
            String surename = order.getUser().getSurename();
            if (false) System.out.println("Usr.surename: " + surename);

            hql = "from PrfOrderItem where deleted = :deleted and order = :order";
            query = session.createQuery(hql);
            query.setParameter("deleted", false);
            query.setParameter("order", order);
            List<PrfOrderItem> items = (List<PrfOrderItem>) query.list();

            for (PrfOrderItem item : items) {
                ++i;
                BigDecimal price  = item.getPrice();
                BigDecimal charge = item.getCharge();
                if (true) {
                    String lang = item.getOrder().getLanguage();
                    String name = item.getUser().getLastname();
                    if (false) System.out.println(">>> Order.lang: " + lang + " User.lastname" + name);
                }
            }
        }

        tr.commit();
        printTime("MULTI SELECT "+i, time1, System.currentTimeMillis());
    }


    /** Update a charge of the order items */
    @SuppressWarnings("unchecked")
    public void useUpdate() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        String hql = "from PrfOrderItem where deleted = :deleted and order.deleted = :deleted";
        Query query = session.createQuery(hql);
        query.setParameter("deleted", false);
        List<PrfOrderItem> items = (List<PrfOrderItem>) query.list();

        int i = 0;
        for (PrfOrderItem item : items) {
            ++i;
            item.setCharge(item.getCharge().add(BigDecimal.ONE));
            session.update(item);
        }

        tr.commit();
        printTime("UPDATE "+i, time1, System.currentTimeMillis());
    }

    /** Create database and using DELETE */
    @SuppressWarnings("unchecked")
    public void useDelete() {

        long time1 = System.currentTimeMillis();
        Transaction tr = session.beginTransaction();

        String hql = "from PrfOrder";
        Query query = session.createQuery(hql);
        List<PrfOrder> orders = (List<PrfOrder>) query.list();

        for (PrfOrder order : orders) {

            hql = "delete from PrfOrderItem it where it.order = :order";
            query = session.createQuery(hql);
            query.setParameter("order", order);
            int rows = query.executeUpdate();

            session.delete(order);
        }

        session.flush();

        hql = "delete from PrfUser";
        query = session.createQuery(hql);
        int rows = query.executeUpdate();

        tr.commit();
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
            BenchmarkHP sample = new BenchmarkHP();

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
        }

        
    }

}
