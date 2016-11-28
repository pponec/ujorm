/*
 *  Copyright 2009-2014 Pavel Ponec
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
package org.ujorm.orm.dialect;

import java.io.IOException;
import java.util.Date;
import junit.framework.TestCase;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.*;
import org.ujorm.orm.bo.*;
import org.ujorm.orm.metaModel.MetaTable;

/**
 * The tests of the SQL LIMIT & OFFSET.
 * @author Pavel Ponec
 */
public class CriterionDialectTest extends TestCase {

    public CriterionDialectTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return CriterionDialectTest.class;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ---------- TESTS -----------------------

    public void testRollbackTranactionA() throws IOException {
        XOrder user = new XOrder();
        user.setId(1L);
        user.setNote("test");
        user.setCreated(new Date());
        //
        final Session session = new OrmHandler(XDatabase.class).createSession();
        session.delete(XItem.ID.forAll());
        session.delete(XOrder.ID.forAll());
        session.save(user);
        session.commit();
        //
        final Criterion<XOrder> crnA, crnB, crnC, criterion;
        crnA = XOrder.ID.whereNotNull();
        crnB = XOrder.ID.whereGt(0L);
        crnC = XOrder.ID.whereLt(0L);
        criterion = crnA.and(crnB.or(crnC));
        //
        long count = session.createQuery(criterion).getCount();
        assertEquals(1L, count);
        //
        String result = getWhere(session.createQuery(criterion));
        assertEquals("WHERE x_ord_order.ID IS NOT NULL AND  (x_ord_order.ID>? OR x_ord_order.ID<?)", result);
    }


    public void testRollbackTranactionB() throws IOException {
        XOrder user = new XOrder();
        user.setId(1L);
        user.setNote("test");
        user.setCreated(new Date());
        //
        final Session session = new OrmHandler(XDatabase.class).createSession();
        session.delete(XItem.ID.forAll());
        session.delete(XOrder.ID.forAll());
        session.save(user);
        session.commit();
        //
        final Criterion<XOrder> crnA, crnB, crnC, criterion;
        crnA = XOrder.CREATED.whereNotNull().and(XOrder.CREATED.whereLt(new Date()));
        crnB = XOrder.NOTE.whereEq("test");
        crnC = XOrder.ID.whereNotNull().and(XOrder.ID.whereLt(100L).or(XOrder.ID.whereGt(200L)));
        criterion = crnA.and(crnB.or(crnC));
        //
        long count = session.createQuery(criterion).getCount();
        assertEquals(1L, count);
        //
        String result = getWhere(session.createQuery(criterion));
        assertEquals("WHERE x_ord_order.CREATED IS NOT NULL AND x_ord_order.CREATED<? "
                + "AND  (x_ord_order.NOTE=? OR x_ord_order.ID IS NOT NULL AND  (x_ord_order.ID<? OR x_ord_order.ID>?) )", result);
    }

    // -----------------------------------------------------

    /** Returns SQL Statement */
    private String getWhere(Query<XOrder> query) throws IOException {
        final String result = getStatement(query);
        int i = 1 + result.lastIndexOf("\tWHERE ");
        return result.substring(i).trim();
    }

    /** Returns SQL Statement */
    private String getStatement(Query<XOrder> query) throws IOException {
        final Criterion<XOrder> criterion = query.getCriterion();
        final Session session = query.getSession();
        final SqlDialect dialect = session.getHandler().getDatabases().get(0).getDialect();
        final MetaTable table = session.getHandler().findTableModel(XOrder.class);
        final String result = dialect.printSelect(table, query, false, new StringBuilder()).toString();

        return result;
    }

    // -----------------------------------------------------

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
