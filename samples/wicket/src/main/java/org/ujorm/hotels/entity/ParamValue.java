/*
 *  Copyright 2014-2014 Pavel Ponec
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

package org.ujorm.hotels.entity;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.DbType;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import static org.ujorm.Validator.Build.*;

/** City with Country */
public class ParamValue extends OrmTable<ParamValue> {

    /** Unique index name */
    private static final String UNIQUE_PARAM_VALUE = "idx_unique_param_value";

    /** Factory */
    private static final KeyFactory<ParamValue> f = newFactory(ParamValue.class);

    /** The primary identifier */
    @Comment("The primary identifier")
    @Column(pk = true)
    public static final Key<ParamValue, Integer> ID = f.newKey();
    /** Parameter Key */
    @Comment("Parameter Key")
    @Column(uniqueIndex=UNIQUE_PARAM_VALUE)
    public static final Key<ParamValue, ParamKey> PARAM_KEY = f.newKey(mandatory());
    /** Related customer or a system customer for a system parameters
     * @see ParamKey#SYSTEM_PARAM */
    @Comment("Related customer or a system customer for a system parameter value")
    @Column(uniqueIndex=UNIQUE_PARAM_VALUE)
    public static final Key<ParamValue, Customer> CUSTOMER = f.newKey(mandatory());
    /** Parameter value in a text format */
    @Comment("Parameter value in a text format")
    @Column(type = DbType.CLOB)
    public static final Key<ParamValue, String> TEXT_VALUE = f.newKey();
    /** Date of the parameter modification */
    @Comment("Date of the last param modification")
    public static final Key<ParamValue, Date> LAST_UPDATE = f.newKey(mandatory());

    static {
        f.lock();
    }

    // --- Getters / Setters ---

    /** The primary identifier */
    public Integer getId() {
        return ID.of(this);
    }

    /** The primary identifier */
    public void setId(Integer id) {
        ParamValue.ID.setValue(this, id);
    }

    /** Parameter Key */
    public ParamKey getParamKey() {
        return PARAM_KEY.of(this);
    }

    /** Parameter Key */
    public void setParamKey(ParamKey paramKey) {
        ParamValue.PARAM_KEY.setValue(this, paramKey);
    }

    /** Related customer or a system customer for a system parameters
     * @see ParamKey#SYSTEM_PARAM */
    public Customer getCustomer() {
        return CUSTOMER.of(this);
    }

    /** Related customer or a system customer for a system parameters
     * @see ParamKey#SYSTEM_PARAM */
    public void setCustomer(Customer customer) {
        ParamValue.CUSTOMER.setValue(this, customer);
    }

    /** Parameter value in a text format */
    public String getTextValue() {
        return TEXT_VALUE.of(this);
    }

    /** Parameter value in a text format */
    public void setTextValue(String textValue) {
        ParamValue.TEXT_VALUE.setValue(this, textValue);
    }

    /** Date of the param modification */
    public Date getLastUpdate() {
        return LAST_UPDATE.of(this);
    }

    /** Date of the param modification */
    public void setLastUpdate(Date lastUpdate) {
        ParamValue.LAST_UPDATE.setValue(this, lastUpdate);
    }

}
