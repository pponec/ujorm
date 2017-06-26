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
package org.ujorm.validator.impl;

import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.validator.AbstractValidator;
import org.ujorm.validator.ValidationError;

/**
 * Not Null alidator
 * @author Pavel Ponec
 */
public class NotNullValidator<VALUE extends Object> extends AbstractValidator<VALUE> {

    /** The default instance of the validator */
    public static final NotNullValidator NOT_NULL = new NotNullValidator();

    public NotNullValidator() {
    }

    /** {@inheritDoc} */
    @Override
    public <UJO extends Ujo> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) {
            final boolean failed = input==null;
            return failed ? createError
                    ( input
                    , key
                    , bo
                    , service.map())
                    : null;
    }

    /** Default Message by template:
     * <br>Value for KEY must not be null
     */
    @Override
    protected String getDefaultTemplate() {
        return service.template("An attribute ", KEY, " must be not null");
    }

    /** @return Returns: "ujorm.org.notNull" */
    public String getLocalizationKey() {
        return KEY_PREFIX + "notNull";
    }
}
