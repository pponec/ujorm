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
package org.ujorm.validator.impl;

import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.validator.AbstractValidator;
import org.ujorm.validator.MessageArg;
import org.ujorm.validator.ValidationError;

/**
 * The Not Null validator, the correct result have an not null input text with a (trimmed) lenght more than zero.
 * @author Pavel Ponec
 */
public class NotEmptyValidator<VALUE extends CharSequence> extends AbstractValidator<VALUE> {

    /** The CharSequence is trimmed */
    public static final MessageArg<Boolean> TRIM = new MessageArg<Boolean>("TRIM");

    /** The CharSequence is trimmed */
    private final boolean trim;


    public NotEmptyValidator() {
        this(false);
    }

    public NotEmptyValidator(boolean trim) {
        this.trim = trim;
    }

    /** {@Inherited} */
    public <UJO extends Ujo> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) {
            final boolean failed = input==null
                    || 0 == (trim
                    ? input.toString().trim()
                    : input).length()
                    ;
            return failed ? new ValidationError
                    ( input
                    , key
                    , bo
                    , getClass()
                    , getLocalizationKey()
                    , getDefaultTemplate()
                    , service.map
                    ( TRIM, trim
                    ))
                    : null;
    }

    /** Default Message by template:
     * <br>The text value KEY must not be empty
     */
    @Override
    protected String getDefaultTemplate() {
        return service.template("The text attribute ", KEY, " must not be empty");
    }


    /** @return Returns: "ujorm.org.notEmpty" */
    public String getLocalizationKey() {
        return KEY_PREFIX + "notEmpty";
    }


}
