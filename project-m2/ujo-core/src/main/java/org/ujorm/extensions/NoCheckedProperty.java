/*
 *  Copyright 2013-2014 Pavel Ponec
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
package org.ujorm.extensions;

import javax.annotation.concurrent.Immutable;
import org.ujorm.Ujo;
import org.ujorm.Validator;
import org.ujorm.core.UjoManager;
import static org.ujorm.extensions.PropertyModifier.*;

/**
 * The implementation of the interface Key where the validator is off (it is never called).
 * @see UjoManager#validate(org.ujorm.Ujo)
 * @author Pavel Ponec
 */
@Immutable
public class NoCheckedProperty<UJO extends Ujo, VALUE> extends Property<UJO, VALUE> {

    /** Protected constructor */
    public NoCheckedProperty(String name, VALUE defaultValue, Validator<VALUE> validator) {
        super(UNDEFINED_INDEX);
        init(NAME, name);
        init(DEFAULT_VALUE, defaultValue);
        init(VALIDATOR, validator);
    }

    /**
     * It is a method for setting an appropriate type safe value to an MapUjo object.
     * The method does not call any validator never!
     */
    @Override
    public void setValue(final UJO ujo, final VALUE value) {
        ujo.writeValue(this, value);
    }
}
