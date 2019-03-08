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
@Service("applParams")
public class ApplicationParams<U extends ApplicationParams> extends AbstractModuleParams {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationParams.class);

    /** Factory */
    private static final KeyFactory<ApplicationParams> f = newCamelFactory(ApplicationParams.class);

    @Comment("Count of rows per a page in the table for a user")
    @PersonalParam
    public static final Key<ApplicationParams, Integer> ROWS_PER_PAGE = f.newKey("RowsPerPage", 10);
    @Comment("Enable a link to java sources")
    public static final Key<ApplicationParams, Boolean> ENABLE_LINK_TO_SOURCES = f.newKey("EnableLinkToSources", true);
    @Comment("Optimized table data loading")
    @PersonalParam
    public static final Key<ApplicationParams, Boolean> OPTIMIZED_TABLES = f.newKey("OptimizedTables", true);
    @Comment("The production application is running")
    public static final Key<ApplicationParams, Boolean> DEBUG_MODE = f.newKey("DebugMode", false);
    @Comment("Parameter Test1 for the system")
    public static final Key<ApplicationParams, String> TEST1 = f.newKey("Test1", "A");
    @Comment("Parameter Test2 for the user")
    @PersonalParam
    public static final Key<ApplicationParams, String> TEST2 = f.newKey("Test2", "B");

    static { f.lock(); }

    @Override
    public ModuleEnum getModule() {
        return ModuleEnum.APPLICATION;
    }

     // --- Generated Getters / Setters powered by: UjoCodeGenerator-1.1.2.nbm ---

    /** Count of rows per a page in the table for a user */
    public Integer getRowsPerPage() {
        return ROWS_PER_PAGE.of(this);
    }

    /** Optimized table data loading */
    public Boolean getOptimizedTables() {
        return OPTIMIZED_TABLES.of(this);
    }

    /** Enable a link to java sources */
    public Boolean getEnableLinkToSources() {
        return ENABLE_LINK_TO_SOURCES.of(this);
    }

    /** Optimized table data loading */
    public boolean isOptimizedTables() {
        final Boolean result = getOptimizedTables();
        return result != null && result;
    }

    /** The production application is running */
    public Boolean getDebugMode() {
        return DEBUG_MODE.of(this);
    }

    /** The production application is running */
    public boolean isDebugMode() {
        final Boolean result = getDebugMode();
        return result != null && result;
    }

    /** Parameter Test1 for the system */
    public String getTest1() {
        return TEST1.of(this);
    }

    /** Parameter Test2 for the user */
    public String getTest2() {
        return TEST2.of(this);
    }

}
