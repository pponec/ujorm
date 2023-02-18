/*
 *  Copyright 2020-2022 Pavel Ponec
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
import org.junit.jupiter.api.Test;
import org.ujorm.orm.ao.IndexModelOrderedBuilder;
import org.ujorm.orm.bo.*;
import org.ujorm.orm.metaModel.MetaIndex;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaTable;

/**
 * The tests of the SQL LIMIT & OFFSET.
 * @author Pavel Ponec
 */
public class ModelTest extends org.junit.jupiter.api.Assertions {

    static private OrmHandler handlerBase;
    static private OrmHandler handlerExtended;


    @Test
    public void testMetaIndexBase() {
        System.out.print("MetaIndex base");
        MetaTable metaTable = getHandlerBase().findTableModel(XOrder.class);
        assertNotNull(metaTable);

        Collection<MetaIndex> indexes = metaTable.getIndexCollection();
        assertEquals(2, indexes.size());
        int count = 0;

        for (MetaIndex index : indexes) {
            if (MetaIndex.NAME.of(index).equals(XOrder.IDX_STATE_NOTE)) {
                assertFalse(MetaIndex.UNIQUE.getValue(index).booleanValue());
                assertEquals(2, index.getColumns().size());
                assertSame(XOrder.STATE, index.getColumns().get(0).getKey());
                assertSame(XOrder.NOTE, index.getColumns().get(1).getKey());
                ++count;
            }
            if (MetaIndex.NAME.of(index).equals(XOrder.IDX_NOTE)) {
                assertFalse(MetaIndex.UNIQUE.getValue(index).booleanValue());
                assertSame(1, index.getColumns().size());
                assertSame(XOrder.NOTE, index.getColumns().get(0).getKey());
                ++count;
            }
        }
        assertEquals(count, indexes.size());
    }

    @Test
    public void testMetaIndexExtended() {
        System.out.print("MetaIndex extended");
        MetaTable metaTable = getHandlerExtended().findTableModel(XOrder.class);
        assertNotNull(metaTable);

        Collection<MetaIndex> indexes = metaTable.getIndexCollection();
        assertEquals(2, indexes.size());
        int count = 0;

        for (MetaIndex index : indexes) {
            if (MetaIndex.NAME.of(index).equals(XOrder.IDX_STATE_NOTE)) {
                assertFalse(MetaIndex.UNIQUE.getValue(index).booleanValue());
                assertEquals(2, index.getColumns().size());
                assertSame(XOrder.STATE, index.getColumns().get(0).getKey());
                assertSame(XOrder.NOTE, index.getColumns().get(1).getKey());
                ++count;
            }
            if (MetaIndex.NAME.of(index).equals(XOrder.IDX_NOTE)) {
                assertFalse(MetaIndex.UNIQUE.getValue(index).booleanValue());
                assertSame(1, index.getColumns().size());
                assertSame(XOrder.NOTE, index.getColumns().get(0).getKey());
                ++count;
            }
        }
        assertEquals(count, indexes.size());
    }

    // ---------- TOOLS -----------------------

    static protected OrmHandler getHandlerBase() {
        if (handlerBase == null) {
            handlerBase = new OrmHandler();
            handlerBase.loadDatabase(XDatabase.class);
        }
        return handlerBase;
    }

    static protected OrmHandler getHandlerExtended() {
        if (handlerExtended == null) {
            handlerExtended = new OrmHandler();

            // There are prefered default keys for a production environment:
            boolean yesIWantToChangeDefaultParameters = true;
            if (yesIWantToChangeDefaultParameters) {
                MetaParams params = new MetaParams();
                params.set(MetaParams.SEQUENCE_SCHEMA_SYMBOL, true);
                params.set(MetaParams.INDEX_MODEL_BUILDER, IndexModelOrderedBuilder.class);
                handlerExtended.config(params);
            }

            handlerExtended.loadDatabase(XDatabase.class);
        }
        return handlerExtended;
    }
}
