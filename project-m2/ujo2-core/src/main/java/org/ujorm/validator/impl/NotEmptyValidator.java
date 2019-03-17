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
import org.ujorm.tools.msg.MessageArg;
import org.ujorm.validator.ValidationError;


/**
 * The not {@code null} validator, the correct result have an not null input text with a (trimmed) length more than zero.
 * @author Pavel Ponec
 */
public class NotEmptyValidator<VALUE extends CharSequence> extends NotNullValidator<VALUE> {

    /** Empty String validator */
    public static final NotEmptyValidator<CharSequence> NOT_EMPTY = new NotEmptyValidator<>(false);
    /** Blank String Validator */
    public static final NotEmptyValidator<CharSequence> NOT_BLANK = new NotEmptyValidator<>(true);


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

    /** {@inheritDoc} */
    @Override
    public <UJO> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) {
            final boolean failed = input==null
                    || 0 == (trim
                    ? input.toString().trim()
                    : input).length()
                    ;
            return failed ? createError
                    ( input
                    , key
                    , bo
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
    @Override
    public String getLocalizationKey() {
        return KEY_PREFIX + "notEmpty";
    }


}
