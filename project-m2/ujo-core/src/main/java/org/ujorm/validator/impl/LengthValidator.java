/*
 *  Copyright 2012-2015 Pavel Ponec
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
import org.ujorm.tools.MessageArg;
import org.ujorm.validator.ValidationError;

/**
 * Validator for a maximal text length. The {@code null} value is allowed, use a {@link NotNullValidator} for the case.
 * @author Pavel Ponec
 */
public class LengthValidator<VALUE extends String> extends AbstractValidator<VALUE> {

    /** String minimal length (inclusive) */
    public static final MessageArg<Integer> MIN = new MessageArg<>("MIN");
    /** String maximal length (inclusive) */
    public static final MessageArg<Integer> MAX = new MessageArg<>("MAX");
    /** Current length of the text value */
    public static final MessageArg<Integer> LENGTH = new MessageArg<>("LENGTH");

    /** String minimal length (inclusive) */
    private final int min;
    /** String maximal length (inclusive) */
    private final int max;

    /**
     * Limit of the legth of the String
     * @param maxLength String maximal length (inclusive)
     */
    public LengthValidator(int maxLength) {
        this(0, maxLength);
    }

    /**
     * Limit of the legth of the String
     * @param min String minimal length (inclusive)
     * @param max String maximal length (inclusive)
     */
    public LengthValidator(int min, int max) {
        this.min = min;
        this.max = max;
    }

    /** {@inheritDoc} */
    @Override
    public <UJO extends Ujo> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) {
        final int length = input != null ? input.length() : min;
        final boolean failed = length < min || length > max;
        return failed ? createError
                ( input
                , key
                , bo
                , service.map
                ( MIN, min
                , MAX, max
                , LENGTH, length
                ))
                : null;
    }

    /** Default Message by template:
     * <br>Text length for KEY must be between MIN, and MAX, but the input length is LENGTH characters
     */
    @Override
    protected String getDefaultTemplate() {
        return service.template("Length of ", KEY, " must be between "
                , MIN, " and ", MAX, ", but the input has ", LENGTH, " characters");
    }

    /**
     * @return Returns a text accoding the constructor argument:
     * <ul>
     *   <li>org.ujorm..maxLimit</li>
     *   <li>org.ujorm..minLimit</li>
     * </ul>
     */
    @Override
    public String getLocalizationKey() {
        return KEY_PREFIX + (min==max
                ? "exactStringLength"
                : "maxStringLength");
    }

    /** String minimal length (inclusive) */
    public int getMinLength() {
        return min;
    }

    /** String maximal length (inclusive) */
    public int getMaxLength() {
        return max;
    }

}
