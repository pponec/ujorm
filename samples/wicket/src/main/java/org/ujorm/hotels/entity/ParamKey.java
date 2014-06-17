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
package org.ujorm.hotels.entity;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.hotels.entity.enums.Module;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import static org.ujorm.Validator.Build.*;

/** Key of persistent parameter */
public class ParamKey extends OrmTable<ParamKey> {

    /** Unique index name */
    private static final String UNIQUE_PARAM_KEY = "idx_unique_param_key";

    /** Factory */
    private static final KeyFactory<ParamKey> f = newFactory(ParamKey.class);

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
    public static final Key<ParamKey, Module> MODULE = f.newKey(mandatory());
    /** The System parameter has a the TRUE value and the User parameter has the FALSE value */
    @Comment("The System parameter has a the TRUE value and the User parameter has the FALSE value")
    public static final Key<ParamKey, Boolean> SYSTEM_PARAM = f.newKeyDefault(true);
    /** Description of the argument */
    @Comment("Description of the argument")
    public static final Key<ParamKey, String> NOTE = f.newKey(length(MANDATORY, 256));
    /** Java class name of the argumetn with no package */
    @Comment("Java class name of the argumetn with no package")
    public static final Key<ParamKey, String> CLASS_NAME = f.newKey(length(MANDATORY, 64));
    /** Java class package of the argument */
    @Comment("Java class package of the argument")
    public static final Key<ParamKey, String> CLASS_PACKAGE = f.newKey(length(MANDATORY, 128));
    /** Date of the param modification */
    @Comment("Date of the last param modification")
    public static final Key<ParamKey, Date> LAST_UPDATE = f.newKey(mandatory());

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
        ParamKey.ID.setValue(this, id);
    }

    /** Parameter module */
    public String getName() {
        return NAME.of(this);
    }

    /** Parameter module */
    public void setName(String name) {
        ParamKey.NAME.setValue(this, name);
    }

    /** Parameter module */
    public Module getModule() {
        return MODULE.of(this);
    }

    /** Parameter module */
    public void setModule(Module module) {
        ParamKey.MODULE.setValue(this, module);
    }

    /** The System parameter (true) or the User parameter (false) */
    public Boolean getSystemParam() {
        return SYSTEM_PARAM.of(this);
    }

    /** The System parameter (true) or the User parameter (false) */
    public void setSystemParam(Boolean systemParam) {
        ParamKey.SYSTEM_PARAM.setValue(this, systemParam);
    }

    /** Description of the argument */
    public String getNote() {
        return NOTE.of(this);
    }

    /** Description of the argument */
    public void setNote(String note) {
        ParamKey.NOTE.setValue(this, note);
    }

    /** Java class name of the argumetn with no package */
    public String getClassName() {
        return CLASS_NAME.of(this);
    }

    /** Java class name of the argumetn with no package */
    public void setClassName(String className) {
        ParamKey.CLASS_NAME.setValue(this, className);
    }

    /** Java class package of the argument */
    public String getClassPackage() {
        return CLASS_PACKAGE.of(this);
    }

    /** Java class package of the argument */
    public void setClassPackage(String classPackage) {
        ParamKey.CLASS_PACKAGE.setValue(this, classPackage);
    }

    /** Date of the param modification */
    public Date getLastUpdate() {
        return LAST_UPDATE.of(this);
    }

    /** Date of the param modification */
    public void setLastUpdate(Date lastUpdate) {
        ParamKey.LAST_UPDATE.setValue(this, lastUpdate);
    }

}
