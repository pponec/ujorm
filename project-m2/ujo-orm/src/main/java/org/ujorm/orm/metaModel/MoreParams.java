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

package org.ujorm.orm.metaModel;

import org.jetbrains.annotations.Unmodifiable;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.orm.AbstractMetaModel;

/**
 * Class contains the special parameters with for different use.
 * @author Pavel Ponec
 */
@Unmodifiable
final public class MoreParams extends AbstractMetaModel {
    private static final Class<MoreParams> CLASS = MoreParams.class;

    /** Property Factory */
    private static final KeyFactory<MoreParams> f = newFactory(CLASS);

    /** A default engine for the MySQL dialect. The default value of this parameter is: "ENGINE = InnoDB".
     * @see org.ujorm.orm.dialect.MySqlDialect#getEngine()
     */
    public static final Key<MoreParams,String> DIALECT_MYSQL_ENGINE_TYPE = f.newKey("dialectMySqlEngineType", "ENGINE = InnoDB");

    /** EFFECTIVA REQUEST: enable to unlock the immutable MetaTable model */
    public static final Key<MoreParams,Boolean> ENABLE_TO_UNLOCK_IMMUTABLE_METAMODEL = f.newKey("enableToUnlockImmutableMeta-model", false);

    /** EFFECTIVA REQUEST: an extended index name strategy where the default value is {@code false}.
     * The {@code true} value makes new index on all foreign keys using the "AUTO" feature.
     * @see MetaColumn#AUTO_INDEX_NAME
     */
    public static final Key<MoreParams,Boolean> EXTENTED_INDEX_NAME_STRATEGY = f.newKey("extentedIndexNameStrategy", false);

    static {
        f.lock(); // Lock the Key factory
    }

}
