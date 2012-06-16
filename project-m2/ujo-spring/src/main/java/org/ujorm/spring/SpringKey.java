/*
 *  Copyright 2012-2012 Pavel Ponec
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

package org.ujorm.spring;

import org.ujorm.extensions.Property;

/**
 * A UjoProperty implementation for a Spring context.
 * @author Pavel Ponec
 */
public class SpringKey<VALUE> extends Property<AbstractAplicationContextAdapter, VALUE> {

    /** Default constructor */
    SpringKey() {
    }
    
    /** Constructor with property name */
    SpringKey(String name) {
        super(name, (VALUE) null, -1);
    }

    
}
