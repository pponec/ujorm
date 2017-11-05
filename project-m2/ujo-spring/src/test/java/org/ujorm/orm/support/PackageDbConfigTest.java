/*
 * Copyright 2017-2017 Pavel Ponec
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
package org.ujorm.orm.support;

import org.junit.Test;
import org.ujorm.KeyList;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.transaction.config.DatabaseModel;
import org.ujorm.transaction.domains.Item;
import static junit.framework.TestCase.assertEquals;

/**
 * PackageDbConfig Test
 * @author Ponec
 */
public class PackageDbConfigTest {

    /** Test of getTableList method, of class PackageDbConfig. */
    @Test
    public void testGetTableList() {
        System.out.println("getTableList");
        Class<?> dbModelClass = DatabaseModel.class;
        PackageDbConfig instance = new PackageDbConfig(dbModelClass);
        KeyList result = instance.getTableList();
        assertEquals(2, result.size());
        assertEquals(dbModelClass, result.getFirstKey().getDomainType());
        assertEquals(Item.class, ((RelationToMany)instance.getTableList().getFirstKey()).getItemType());

    }

}
