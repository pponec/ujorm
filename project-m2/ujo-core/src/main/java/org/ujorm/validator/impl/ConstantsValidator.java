/*
 *  Copyright 2013-2026 Pavel Ponec
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.tools.msg.MessageArg;
import org.ujorm.validator.AbstractValidator;
import org.ujorm.validator.ValidationError;

/**
 * Validator compares an input value with a set of constants by the hashCode() and equals() methods.
 * @author Pavel Ponec
 */
public class ConstantsValidator<VALUE> extends AbstractValidator<VALUE> {

    /** Set of constant to a check */
    public static final MessageArg<Set> SET = MessageArg.of("SET");
    /** Constants are forbidden / required */
    public static final MessageArg<Boolean> FORBIDDEN = MessageArg.of("FORBIDDEN");

    /** Set of constant to a check */
    private final Set set;
    /** Constants are forbidden / required */
    private final boolean forbidden;

    /**
     * Validator compares an input value with a set of constants by the hashCode() and equals() methods.
     * @param forbidden A sign if the input value is a forbidden ({@code true}) or required ({@code false}) set.
     * @param values Serializable values must have implemented both methods: equals() and hashCode().
     * and value {@code false} means minimal limit (inclusive).
     */
    public ConstantsValidator(boolean forbidden, VALUE ... values) {
        this.forbidden = forbidden;
        this.set = new HashSet(values.length);

        Collections.addAll(set, values);
    }

    /** {@inheritDoc} */
    @Override
    public <UJO extends Ujo> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) {
            final boolean ok = input==null
                    || (forbidden != set.contains(input));
            return !ok ? createError
                    ( input
                    , key
                    , bo
                    , service.map
                    ( SET, Collections.unmodifiableSet(set)
                    , FORBIDDEN, forbidden
                    ))
                    : null;
    }

    /** Default Message Template.
     * <br>Value for KEY must be less/great than or equals to the LIMIT but the input is: INPUT
     */
    @Override
    protected String getDefaultTemplate() {
        final String result;
        if (forbidden) {
            result = service.template("The value ", INPUT
                    , " for the ", KEY
                    , " must not be from the forbidden set: ", SET);
        } else {
            result = service.template("The value ", INPUT
                    , " for the ", KEY
                    , " must be one from the required set: ", SET);
        }
        return result;
    }

    /**
     * @return Returns a text accoding the constructor argument:
     * <ul>
     *   <li>org.ujorm.values.forbidden</li>
     *   <li>org.ujorm.values.required</li>
     * </ul>
     */
    @Override
    public String getLocalizationKey() {
        return KEY_PREFIX + "constants." + (forbidden ? "forbidden" : "required");
    }

}
