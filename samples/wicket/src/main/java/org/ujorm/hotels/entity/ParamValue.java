/*
 *  Copyright 2014-2022 Pavel Ponec
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

import java.time.LocalDateTime;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.hotels.entity.enums.ModuleEnum;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.DbType;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.annot.Table;
import static org.ujorm.validator.impl.ValidatorFactory.mandatory;

/** Parameter value mapping to database */
@Table("param_value")
public final class ParamValue extends OrmTable<ParamValue> {

    /** Unique index name */
    private static final String UNIQUE_PARAM_VALUE = "idx_unique_param_value";

    /** Factory */
    private static final KeyFactory<ParamValue> f = newCamelFactory(ParamValue.class);

    /** The identifier must have an ascending sort for personal rows. */
    @Comment("The identifier must have an ascending sort for defautl personal rows")
    @Column(pk = true)
    public static final Key<ParamValue, Integer> ID = f.newKey();
    /** Parameter Key where the NULL value means a 'system parameter' */
    @Comment("Parameter Key where the NULL value means a 'system parameter'")
    @Column(uniqueIndex=UNIQUE_PARAM_VALUE, mandatory = true)
    public static final Key<ParamValue, ParamKey> PARAM_KEY = f.newKey();
    /** Related customer or the null value for the system parameter
     * @see ParamKey#SYSTEM_PARAM */
    @Comment("Related customer or the null value for the system parameter")
    @Column(uniqueIndex=UNIQUE_PARAM_VALUE)
    public static final Key<ParamValue, Customer> CUSTOMER = f.newKey();
    /** Parameter value in a text format */
    @Comment("Parameter value in a text format")
    @Column(type = DbType.CLOB)
    public static final Key<ParamValue, String> TEXT_VALUE = f.newKey();
    /** Date of the parameter modification */
    @Comment("Date of the last param modification")
    public static final Key<ParamValue, LocalDateTime> LAST_UPDATE = f.newKey(mandatory(LocalDateTime.class));

    static {
        f.lock();
    }

    // --- Composite keys ---

    /** Composite Key ID */
    public static final Key<ParamValue, Integer> KEY_ID$ = PARAM_KEY.add(ParamKey.ID);
    /** Composite KeyName */
    public static final Key<ParamValue, String> KEY_NAME$ = PARAM_KEY.add(ParamKey.NAME);
    /** Composite KeyModule */
    public static final Key<ParamValue, ModuleEnum> KEY_MODULE$ = PARAM_KEY.add(ParamKey.MODULE);
    /** System parameter */
    public static final Key<ParamValue, Boolean> KEY_SYSTEM$ = PARAM_KEY.add(ParamKey.SYSTEM_PARAM);

    // --- Constructors ---

    /** Default constructor */
    public ParamValue() {
    }

    /** Key constructor assign paramKey and a text value */
    public ParamValue(final ParamKey paramKey) {
        setParamKey(paramKey);
        setTextValue(paramKey.getTextDefaultValue());
    }

    /** Is the parametry a Personal type of */
    public boolean isPersonalParam() {
        return !KEY_SYSTEM$.of(this);
    }

    // --- Generated Getters / Setters powered by: UjoCodeGenerator-1.1.2.nbm ---

    /** The identifier must have an ascending sort for personal rows. */
    public Integer getId() {
        return ID.of(this);
    }

    /** The identifier must have an ascending sort for personal rows. */
    public void setId(Integer id) {
        ID.setValue(this, id);
    }

    /** Parameter Key where the NULL value means a 'system parameter' */
    public ParamKey getParamKey() {
        return PARAM_KEY.of(this);
    }

    /** Parameter Key where the NULL value means a 'system parameter' */
    public void setParamKey(ParamKey paramKey) {
        PARAM_KEY.setValue(this, paramKey);
    }

    /** Related customer or the null value for the system parameter
     * @see ParamKey#SYSTEM_PARAM */
    public Customer getCustomer() {
        return CUSTOMER.of(this);
    }

    /** Related customer or the null value for the system parameter
     * @see ParamKey#SYSTEM_PARAM */
    public void setCustomer(Customer customer) {
        CUSTOMER.setValue(this, customer);
    }

    /** Parameter value in a text format */
    public String getTextValue() {
        return TEXT_VALUE.of(this);
    }

    /** Parameter value in a text format */
    public void setTextValue(String textValue) {
        TEXT_VALUE.setValue(this, textValue);
    }

    /** Date of the parameter modification */
    public LocalDateTime getLastUpdate() {
        return LAST_UPDATE.of(this);
    }

    /** Date of the parameter modification */
    public void setLastUpdate(LocalDateTime lastUpdate) {
        LAST_UPDATE.setValue(this, lastUpdate);
    }

}
