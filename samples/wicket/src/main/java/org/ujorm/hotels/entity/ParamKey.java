/*
 *  Copyright 2020-2026 Pavel Ponec
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
import org.jetbrains.annotations.NotNull;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.hotels.entity.enums.ModuleEnum;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.DbType;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.annot.Table;
import static org.ujorm.validator.impl.ValidatorFactory.MANDATORY;
import static org.ujorm.validator.impl.ValidatorFactory.length;
import static org.ujorm.validator.impl.ValidatorFactory.mandatory;

/** Key of persistent parameter */
@Table("param_key")
public final class ParamKey extends OrmTable<ParamKey> {

    /** Unique index name */
    private static final String UNIQUE_PARAM_KEY = "idx_unique_param_key";

    /** Factory */
    private static final KeyFactory<ParamKey> f = newCamelFactory(ParamKey.class);

    /** The primary identifier */
    @Comment("The primary identifier")
    @Column(pk = true)
    public static final Key<ParamKey, Integer> ID = f.newKey();
    /** Parameter key name*/
    @Comment("Parameter key name")
    @Column(uniqueIndex=UNIQUE_PARAM_KEY)
    public static final Key<ParamKey, String> NAME = f.newKey(length(MANDATORY, 128));
    /** Parameter module */
    @Comment("Parameter module")
    @Column(name="module_code", uniqueIndex=UNIQUE_PARAM_KEY)
    public static final Key<ParamKey, ModuleEnum> MODULE = f.newKey(mandatory(ModuleEnum.class));
    /** The System parameter has a the TRUE value and the User parameter has the FALSE value */
    @Comment("The System parameter has a the TRUE value and the User parameter has the FALSE value")
    public static final Key<ParamKey, Boolean> SYSTEM_PARAM = f.newKeyDefault(true);
    /** Description of the argument */
    @Comment("Description of the argument")
    public static final Key<ParamKey, String> NOTE = f.newKey(length(MANDATORY, 256));
    /** Java class name of the argument with no package */
    @Comment("Java class name of the argument with no package")
    public static final Key<ParamKey, String> CLASS_NAME = f.newKey(length(MANDATORY, 64));
    /** Java class package of the argument */
    @Comment("Java class package of the argument")
    public static final Key<ParamKey, String> CLASS_PACKAGE = f.newKey(length(MANDATORY, 128));
    /** Parameter default value in a text format */
    @Comment("Parameter default value in a text format")
    @Column(type = DbType.CLOB)
    public static final Key<ParamKey, String> TEXT_DEFAULT_VALUE = f.newKey();
    /** Date of the parameter modification */
    @Comment("Date of the last parameter modification")
    public static final Key<ParamKey, LocalDateTime> LAST_UPDATE = f.newKey(mandatory(LocalDateTime.class));

    static {
        f.lock();
    }

    /** Default constructor */
    public ParamKey() {
    }

    /** Key constructor */
    public ParamKey(@NotNull final String name, @NotNull final ModuleEnum module) {
        setName(name);
        setModule(module);
    }

    /** Save a class of parameter value */
    public void setParamClass(Class<?> type) {
        CLASS_NAME.setValue(this, type.getSimpleName());
        CLASS_PACKAGE.setValue(this, type.getPackage().getName());
    }

    /** Get a class of parameter value */
    public Class<?> getParamClass() throws IllegalStateException {
        final String type = CLASS_PACKAGE.of(this) + '.' + CLASS_NAME.of(this);
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class was not found: " + type, e);
        }
    }

    // --- Generated Getters / Setters powered by: UjoCodeGenerator-1.1.2.nbm ---

    /** The primary identifier */
    public Integer getId() {
        return ID.of(this);
    }

    /** The primary identifier */
    public void setId(Integer id) {
        ID.setValue(this, id);
    }

    /** Parameter key name*/
    public String getName() {
        return NAME.of(this);
    }

    /** Parameter key name*/
    public void setName(String name) {
        NAME.setValue(this, name);
    }

    /** Parameter module */
    public ModuleEnum getModule() {
        return MODULE.of(this);
    }

    /** Parameter module */
    public void setModule(ModuleEnum module) {
        MODULE.setValue(this, module);
    }

    /** The System parameter has a the TRUE value and the User parameter has the FALSE value */
    public Boolean getSystemParam() {
        return SYSTEM_PARAM.of(this);
    }

    /** The System parameter has a the TRUE value and the User parameter has the FALSE value */
    public void setSystemParam(Boolean systemParam) {
        SYSTEM_PARAM.setValue(this, systemParam);
    }

    /** Description of the argument */
    public String getNote() {
        return NOTE.of(this);
    }

    /** Description of the argument */
    public void setNote(String note) {
        NOTE.setValue(this, note);
    }

    /** Java class name of the argument with no package */
    public String getClassName() {
        return CLASS_NAME.of(this);
    }

    /** Java class name of the argument with no package */
    public void setClassName(String className) {
        CLASS_NAME.setValue(this, className);
    }

    /** Java class package of the argument */
    public String getClassPackage() {
        return CLASS_PACKAGE.of(this);
    }

    /** Java class package of the argument */
    public void setClassPackage(String classPackage) {
        CLASS_PACKAGE.setValue(this, classPackage);
    }

    /** Parameter default value in a text format */
    public String getTextDefaultValue() {
        return TEXT_DEFAULT_VALUE.of(this);
    }

    /** Parameter default value in a text format */
    public void setTextDefaultValue(String textDefaultValue) {
        TEXT_DEFAULT_VALUE.setValue(this, textDefaultValue);
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
