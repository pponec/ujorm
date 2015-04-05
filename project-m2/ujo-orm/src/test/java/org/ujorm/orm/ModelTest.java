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
package org.ujorm.orm;

import java.util.Collection;
import junit.framework.TestCase;
import org.ujorm.orm.bo.*;
import org.ujorm.orm.metaModel.MetaIndex;
import org.ujorm.orm.metaModel.MetaTable;

/**
 * The tests of the SQL LIMIT & OFFSET.
 * @author Pavel Ponec
 */
public class ModelTest extends TestCase {

    static private OrmHandler handler;

    public ModelTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return ModelTest.class;
    }

    // ---------- TESTS -----------------------

    public void testMetaIndex() {
        System.out.print("MetaIndex");
        MetaTable metaTable = getHandler().findTableModel(XOrder.class);
        assertNotNull(metaTable);

        Collection<MetaIndex> indexes = metaTable.getIndexCollection();
        assertEquals(2, indexes.size());
        int count = 0;

        for (MetaIndex index : indexes) {
            if (MetaIndex.NAME.of(index).equals(XOrder.IDX_STATE_NOTE)) {
                assertEquals(false, MetaIndex.UNIQUE.getValue(index).booleanValue());
                assertEquals(2, index.getColumns().size());
                assertSame(XOrder.STATE, index.getColumns().get(0).getKey());
                assertSame(XOrder.NOTE, index.getColumns().get(1).getKey());
                ++count;
            }
            if (MetaIndex.NAME.of(index).equals(XOrder.IDX_NOTE)) {
                assertEquals(false, MetaIndex.UNIQUE.getValue(index).booleanValue());
                assertSame(1, index.getColumns().size());
                assertSame(XOrder.NOTE, index.getColumns().get(0).getKey());
                ++count;
            }
        }
        assertEquals(count, indexes.size());
    }

    // ---------- TOOLS -----------------------

    static protected OrmHandler getHandler() {
        if (handler == null) {
            handler = new OrmHandler();
            handler.loadDatabase(XDatabase.class);
        }
        return handler;
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
