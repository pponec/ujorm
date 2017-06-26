/*
 *  Copyright 2009-2010 Tomas Hampl
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

package org.ujorm.hibernateSupport;

import org.hibernate.PropertyNotFoundException;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.Setter;

/**
 * A support for the <a href="https://www.hibernate.org/" target="_top">Hibernate</a> framework.
 * See the <a href="package-summary.html#Hibernate">description</a> for more information.
 * @author hampl
 */
public class UjoPropertyAccessor implements PropertyAccessor {

    @Override
    public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return new UjoPropertyGetter(propertyName,theClass);
    }

    @Override
    public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return new UjoPropertySetter(propertyName);
    }
}
