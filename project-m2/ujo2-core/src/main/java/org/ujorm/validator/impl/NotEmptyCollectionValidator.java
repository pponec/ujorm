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

import java.util.Collection;
import org.ujorm.Key;
import org.ujorm.validator.AbstractValidator;
import org.ujorm.validator.ValidationError;


/**
 * Not empty Collection validator
 * @author Pavel Ponec
 */
public class NotEmptyCollectionValidator<VALUE extends Collection> extends AbstractValidator<VALUE> {

    protected NotEmptyCollectionValidator() {
    }
    /** {@inheritDoc} */
    public <UJO> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) {
            final boolean failed = input==null || input.size()==0;
            return failed ? createError
                    ( input
                    , key
                    , bo
                    , service.map())
                    : null;
    }

    /** Default Message by template:
     * <br>The collection value KEY, must not be empty
     */
    @Override
    protected String getDefaultTemplate() {
        return service.template("The collection attribute ", KEY, " must not be empty");
    }

    /** @return Return: "org.ujorm.notEmptyCollection" */
    @Override
    public String getLocalizationKey() {
        return KEY_PREFIX + "notEmptyCollection";
    }


}
