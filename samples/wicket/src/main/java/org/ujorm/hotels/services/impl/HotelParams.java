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
package org.ujorm.hotels.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.hotels.entity.enums.Module;
import org.ujorm.hotels.services.annot.PersonalParam;
import org.ujorm.orm.annot.Comment;
/**
 * Common database service implementations
 * @author Ponec
 */
@Service("hotelParams")
public class HotelParams<U extends HotelParams> extends AbstractModuleParams {
    private static final Logger LOGGER = LoggerFactory.getLogger(HotelParams.class);

    /** Factory */
    private static final KeyFactory<HotelParams> f = newFactory(HotelParams.class);

    @Comment("Count of rows per a page in the table")
    public static final Key<HotelParams, Integer> ROWS_PER_PAGE = f.newKey("RowsPerPage", 10);
    public static final Key<HotelParams, String> TEST1 = f.newKey("Test1", "A");
    public static final Key<HotelParams, String> TEST2 = f.newKey("Test2", "B");
    @PersonalParam
    public static final Key<HotelParams, String> TEST3 = f.newKey("Test3", "C");

    static { f.lock(); }

    @Override
    public Module getModule() {
        return Module.HOTELS;
    }

    //<editor-fold defaultstate="collapsed" desc="Generated getters">
    public Integer getRowsPerPage() {
        return ROWS_PER_PAGE.of(this);
    }
    //</editor-fold>

}
