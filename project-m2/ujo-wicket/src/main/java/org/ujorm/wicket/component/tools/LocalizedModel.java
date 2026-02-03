/*
 *  Copyright 2014-2026 Pavel Ponec
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
package org.ujorm.wicket.component.tools;

import org.apache.wicket.model.ResourceModel;
import org.ujorm.Key;

/**
 * Extended {@link ResourceModel} where default value is the original key by default.
 * @author Pavel Ponec
 */
public class LocalizedModel extends ResourceModel {

    /** Default value is the same as key */
    public LocalizedModel(String resourceKey) {
        super(resourceKey, resourceKey);
    }

    public LocalizedModel(String resourceKey, String defaultValue) {
        super(resourceKey, defaultValue);
    }

    /** Get a full key name with no alias information by example:  */
    public static final String getSimpleKeyName(final Key key) {
        if (key == null) {
            return "undefined.key";
        }
        final Class domainType = key.getDomainType();
        return domainType != null
             ? domainType.getSimpleName() + '.' +  key.getName()
             : key.getName();
    }

}
