/*
 * Copyright 2014, Pavel Ponec
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
package org.ujorm.hotels.service.param;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.hotels.entity.enums.ModuleEnum;
import org.ujorm.hotels.service.impl.AbstractModuleParams;
import org.ujorm.hotels.service.param.annot.PersonalParam;
import org.ujorm.orm.annot.Comment; 
/**
 * Common database service implementations
 * @author Ponec
 */
@Service("testParams")
public class TestParams<U extends TestParams> extends AbstractModuleParams {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestParams.class);

    /** Factory */
    private static final KeyFactory<TestParams> f = newFactory(TestParams.class);

    @Comment("Count of rows per a page in the table")
    public static final Key<TestParams, Integer> ROWS_PER_PAGE = f.newKey("RowsPerPage", 10);
    public static final Key<TestParams, String> TEST1 = f.newKey("Test1", "A");
    public static final Key<TestParams, String> TEST2 = f.newKey("Test2", "B");
    @PersonalParam
    public static final Key<TestParams, String> TEST3 = f.newKey("Test3", "C");

    static { f.lock(); }

    @Override
    public ModuleEnum getModule() {
        return ModuleEnum.SYSTEM;
    }

     // --- Generated Getters / Setters powered by: UjoCodeGenerator-1.1.2.nbm ---

    /** Count of rows per a page in the table */
    public Integer getRowsPerPage() {
        return ROWS_PER_PAGE.of(this);
    }

    public String getTest1() {
        return TEST1.of(this);
    }

    public String getTest2() {
        return TEST2.of(this);
    }

    public String getTest3() {
        return TEST3.of(this);
    }
}
