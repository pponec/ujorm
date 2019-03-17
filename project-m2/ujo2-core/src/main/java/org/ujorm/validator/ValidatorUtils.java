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
package org.ujorm.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.ujorm.Validator;
import org.ujorm.criterion.BinaryOperator;
import org.ujorm.validator.impl.CompositeValidator;
import org.ujorm.validator.impl.LengthValidator;
import org.ujorm.validator.impl.NotNullValidator;

/**
 * Validator utils
 * @author Pavel Ponec
 */
public final class ValidatorUtils {

    /** Returns the true value if the validator contains a not-null validator.
     * @param validator Nullable validator
     * @return Returns the true value if the validator contains a not-null validator.
     */
    public static boolean isMandatoryValidator(Validator validator) {
        List<LengthValidator> vals = new ArrayList<>();
        boolean ok = findValidators(validator, NotNullValidator.class, vals);
        return ok && !vals.isEmpty();
    }

    /** Returns the maximal lenght from all LengthValidators.
     * @param validator Nullable validator
     * @return The undefined value is -1.
     */
    public static int getMaxLength(Validator validator) {
        int result = -1;
        List<LengthValidator> vals = new ArrayList<>();
        findValidators(validator, LengthValidator.class, vals);

        for (LengthValidator v : vals) {
            result = Math.max(result, v.getMaxLength());
        }
        return result;
    }

    /** Find a required validators
     * @param validator Nullable validator
     * @param requiredType requiredType
     * @param validators Result list of validators
     * @return return true if all operators are AND.
     */
    @SuppressWarnings("unchecked")
    protected static boolean findValidators(Validator validator, Class<? extends Validator> requiredType, List<? extends Validator> validators) {
        boolean and = true;
        if (validator instanceof CompositeValidator) {
            CompositeValidator cv = (CompositeValidator) validator;
            and = cv.getOperator() == BinaryOperator.AND;
            and = and && findValidators(cv.getLeftValidator(), requiredType, validators);
            and = and && findValidators(cv.getRightValidator(), requiredType, validators);
        } else if (requiredType.isInstance(validator)) {
            ((List<Validator>)validators).add(validator);
        }
        return and;
    }

    /** Validate the argument using all keys from the collection
     * where the ReadOnlyValidator validators are excluded.
     * ujos Collection of the beans
     */
    public static List<ValidationError> validate(final Object ujo) {
            throw new UnsupportedOperationException("TODO");

    }

    /** Validate the argument using all keys from the collection
     * where the ReadOnlyValidator validators are excluded.
     * ujos Collection of the beans
     */
    public static List<ValidationError> validate(final Collection<Object> ujos) {
        final ArrayList<ValidationError> result = new ArrayList<>();
            throw new UnsupportedOperationException("TODO");

    }
}
