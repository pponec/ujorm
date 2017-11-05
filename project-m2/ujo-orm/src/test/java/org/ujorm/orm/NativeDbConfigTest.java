/*
 * Copyright 2017 pavel.
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

import org.junit.Test;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.bo.XDatabase;
import org.ujorm.orm.bo.XOrder;
import static junit.framework.TestCase.assertEquals;

/**
 *
 * @author Pavel Ponec
 */
public class NativeDbConfigTest {

    /**
     * Test of getDbModel method, of class NativeDbConfig.
     */
    @Test
    public void testGetDbModel() {
        System.out.println("getDbModel");
        NativeDbConfig instance = new NativeDbConfig(XDatabase.class);
        assertEquals(XDatabase.class, instance.getDbModel().getClass());
        assertEquals(XOrder.class, ((RelationToMany)instance.getTableList().getFirstKey()).getItemType());

    }

}
