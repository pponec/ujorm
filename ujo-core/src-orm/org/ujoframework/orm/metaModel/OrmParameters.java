/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.orm.metaModel;

import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.AbstractMetaModel;
import org.ujoframework.orm.metaModel.ao.Cache;

/**
 * A logical database description.
 * The class is a root of database configuration.
 * @author Pavel Ponec
 */
public class OrmParameters extends AbstractMetaModel {

    public static final Logger LOGGER = Logger.getLogger(OrmParameters.class.getName());

    /** Enabnle / desable a session cache */
    public static final UjoProperty<OrmParameters,Cache> CACHE = newProperty("cache", Cache.NONE);


}
