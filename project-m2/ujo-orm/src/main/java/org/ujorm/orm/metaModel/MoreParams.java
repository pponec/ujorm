/*
 *  Copyright 2009-2010 Pavel Ponec
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

import org.ujorm.core.annot.Immutable;
import org.ujorm.logger.UjoLogger;
import org.ujorm.extensions.Property;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.AbstractMetaModel;

/**
 * Class contains the special parameters with for different use.
 * @author Pavel Ponec
 */
@Immutable
final public class MoreParams extends AbstractMetaModel {
    private static final Class CLASS = MoreParams.class;
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(MoreParams.class);

    /** A default engine for the MySQL dialect. The default value of this parameter is: "ENGINE = InnoDB".
     * @see org.ujorm.orm.dialect.MySqlDialect#getEngine()
     */
    public static final Property<MoreParams,String> DIALECT_MYSQL_ENGINE_TYPE = newProperty("DialectMySqlEngineType", "ENGINE = InnoDB");

}
