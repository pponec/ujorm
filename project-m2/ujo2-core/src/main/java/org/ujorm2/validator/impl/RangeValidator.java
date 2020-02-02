/*
 *  Copyright 2012-2014 Pavel Ponec
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
package org.ujorm2.validator.impl;

import org.ujorm2.Key;
import org.ujorm2.validator.ValidationError;


/**
 * Range alidator for values form (inclusive) to (inclusive).
 * @author Pavel Ponec
 * @see BetweenValidator
 */
public class RangeValidator<VALUE extends Comparable> extends BetweenValidator<VALUE> {

    /**
     * Between validator
     * @param min Serializable minimum (inclusive)
     * @param max Serializable maximum (inclusive)
     */
    public RangeValidator(VALUE min, VALUE max) {
        super(min, max);
    }

    /** {@inheritDoc} */
    @Override
    public <D> ValidationError validate(VALUE input, Key<D, VALUE> key, D bo) {
            final boolean ok = input==null
                    || input.compareTo(min) >= 0
                    && input.compareTo(max) <= 0;
            return !ok ? createError
                    ( input
                    , key
                    , bo
                    , service.map
                    ( MIN, min
                    , MAX, max
                    ))
                    : null;
    }

    /** Returns a default message by template:
     * <br>"Value for KEY must be between MIN and MAX but the input is: INPUT */
    @Override
    protected String getDefaultTemplate() {
        return service.template("An attribute ", KEY, " must be between "
                , MIN, " and ", MAX, " (including), but the input is: ", INPUT);
    }

    /** Default value is: "org.ujorm.between" by template */
    @Override
    public String getLocalizationKey() {
        return KEY_PREFIX + "range";
    }
}
