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
import org.ujorm.criterion.Criterion;
import org.ujorm.validator.AbstractValidator;
import org.ujorm.validator.MessageArg;
import org.ujorm.validator.ValidationError;

/**
 * Validator for the Ujo Criteiron.
 * <br>Note: The validator is not serializable directly, because the Criterion is not serializable object.
 * @author Pavel Ponec
 */
public class CriterionValidator<VALUE extends Ujo> extends AbstractValidator<VALUE> {

    public static final MessageArg<String> CRN = new MessageArg<String>("CRN");

    /** Criterion to validation */
    private final Criterion<VALUE> crn;

    protected CriterionValidator(Criterion<VALUE> crn) {
        this.crn = crn;
    }
    /** {@inheritDoc} */
    public <UJO extends Ujo> ValidationError validate(VALUE input, Key<UJO, VALUE> key, UJO bo) {
            final boolean ok = input==null
                    || crn.evaluate(input);
            return !ok ? createError
                    ( input
                    , key
                    , bo
                    , service.map
                    ( CRN, crn.toString()
                    ))
                    : null;
    }

    /** Default Message by template:
     * <br>Value for KEY is not valid for the condition: CRN,.The input object is: INPUT
     */
    @Override
    protected String getDefaultTemplate() {
        return service.template("An attribute ", KEY, " is not valid for the condition: "
                , CRN,". The input object is: ", INPUT);
    }

    /** @return Retunrs: "org.ujorm.criterion" */
    public String getLocalizationKey() {
        return KEY_PREFIX + "criterion";
    }

    /** @{@inheritDoc} */
    @Override
    public String toString() {
        return super.toString() + ": " + crn.toString();
    }

}
