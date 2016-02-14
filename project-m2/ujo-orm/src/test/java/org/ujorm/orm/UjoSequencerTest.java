/*
 * Copyright 2013 Pavel Ponec.
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
package org.ujorm.orm;

import org.ujorm.orm_tutorial.sample.entity.Order;
import org.ujorm.orm_tutorial.sample.entity.Database;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.ujorm.Ujo;
import org.ujorm.logger.UjoLogger;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.orm_tutorial.sample.*;

/**
 * Test for the Ujorm sequence
 * @author ponec
 */
public class UjoSequencerTest extends TestCase {

    public UjoSequencerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of nextValue method, of class UjoSequencer.
     */
    public void testNextValue_1() {
        System.out.println("testNextValue_1");

        final int sequenceCache = 1;
        final OrmHandler handler = createHandler(sequenceCache);
        final MetaTable orderModel = handler.findTableModel(Order.class);
        final UjoSequencer seq = orderModel.getSequencer();
        final Session session = handler.createSession();

        long seqBeg = seq.nextValue(session);
        long seqEnd = 0;
        int count = 51;
        for (int i = 0; i < count; i++) {
            seqEnd =  seq.nextValue(session);
        }
        session.close();
        assertEquals("ID sequences must be the same", seqBeg + count, seqEnd);
    }

    /**
     * Test of nextValue method, of class UjoSequencer.
     */
    public void testNextValue_2() {
        System.out.println("testNextValue_2");

        final int sequenceCache = 2;
        final OrmHandler handler = createHandler(sequenceCache);
        final MetaTable orderModel = handler.findTableModel(Order.class);
        final UjoSequencer seq = orderModel.getSequencer();
        final Session session = handler.createSession();

        long seqBeg = seq.nextValue(session);
        long seqEnd = 0;
        int count = 52;
        for (int i = 0; i < count; i++) {
            seqEnd =  seq.nextValue(session);
        }
        session.close();
        assertEquals("ID sequences must be the same", seqBeg + count, seqEnd);
    }

    /**
     * Test of nextValue method, of class UjoSequencer.
     */
    public void testNextValue_3() {
        System.out.println("testNextValue_3");

        final int sequenceCache = 3;
        final OrmHandler handler = createHandler(sequenceCache);
        final Session session = handler.createSession();

        Order orderBeg = createAndSaveOrder(session);
        Order orderEnd = null;
        int count = 53;
        for (long i = 0; i < count; i++) {
            orderEnd = createAndSaveOrder(session);
        }
        session.close();
        assertEquals("ID sequences must be the same"
                , orderBeg.getId().longValue() + count
                , orderEnd.getId().longValue());
    }

    /** Create and Save Order to databaze */
    private Order createAndSaveOrder(Session session) {
        final Order result = new Order();
        result.setNote("test");
        session.save(result);
        return result;
    }

    /** Before the first: create a meta-model.
     * Database tables will be CREATED in the first time.
     */
    private OrmHandler createHandler(int sequenceCache) {

        // Set the log level specifying which message levels will be logged by Ujorm:
        Logger.getLogger(Ujo.class.getPackage().getName()).setLevel(UjoLogger.ERROR);

        // Create new ORM Handler:
        OrmHandler result = new OrmHandler();

        // There are prefered default keys for a production environment:
        boolean yesIWantToChangeDefaultParameters = true;
        if (yesIWantToChangeDefaultParameters) {
            MetaParams params = new MetaParams();
            params.set(MetaParams.SEQUENCE_SCHEMA_SYMBOL, true);
            params.set(MetaParams.SEQUENCE_CACHE, sequenceCache);
            params.set(MetaParams.LOG_METAMODEL_INFO, false);
            params.setQuotedSqlNames(false);
            result.config(params);
        }

        // Load Meta-model and lock it to a read-only mode:
        result.loadDatabase(Database.class);

        return result;
    }


}
