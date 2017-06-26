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
import org.ujorm.validator.MessageArg;
import org.ujorm.validator.ValidationError;

/**
 * Comparable Validator
 * @author Pavel Ponec
 */
public class ComparableValidator<VALUE extends Comparable> extends AbstractValidator<VALUE> {

    public static final MessageArg<Comparable> LIMIT = new MessageArg<>("LIMIT");
    /** Sing for MAX/MIN */
    public static final MessageArg<Boolean> MAX = new MessageArg<>("MAX");

    /** Serializable minimum (inclusive) */
    private final Comparable limit;
    /** maximal limit */
    private final boolean max;

    /**
     * Between validator
     * @param limit Serializable minimum (inclusive)
     * @param maxLimit The {@code true} value means maximal limit (inclusive)
     * and value {@code false} means minimal limit (inclusive).
     */
    public ComparableValidator(VALUE limit, boolean maxLimit) {
        this.limit = limit;
        this.max = maxLimit;
    }

    /** {@inheritDoc} */
    @Override
    public <UJO extends Ujo> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) {
            final boolean ok = input==null
                    || ( max
                    ? input.compareTo(limit) <= 0
                    : input.compareTo(limit) >= 0 )
                    ;
            return !ok ? createError
                    ( input
                    , key
                    , bo
                    , service.map
                    ( LIMIT, limit
                    , MAX, max
                    ))
                    : null;
    }

    /** Default Message Template.
     * <br>Value for KEY must be less/great than or equals to the LIMIT but the input is: INPUT
     */
    @Override
    protected String getDefaultTemplate() {
        final String diffName = max ? "less" : "great";
        return service.template("An attribute ", KEY
                , " must be ", diffName, " than or equals to the ", LIMIT
                , ", but the input is: ", INPUT);
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
        return KEY_PREFIX + (max ? "maxLimit" : "minLimit");
    }

}
