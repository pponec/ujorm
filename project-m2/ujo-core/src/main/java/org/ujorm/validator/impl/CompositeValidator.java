/*
 *  Copyright 2012-2022 Pavel Ponec
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
import org.ujorm.Validator;
import org.ujorm.criterion.BinaryOperator;
import org.ujorm.validator.AbstractValidator;
import org.ujorm.validator.ValidationError;

/**
 * Composite validator joins two another validators using the one operator from AND / OR.
 * @author Pavel Ponec
 */
final public class CompositeValidator<VALUE> extends AbstractValidator<VALUE> {

    private final Validator<VALUE> leftValidator;
    private final Validator<VALUE> rightValidator;
    private final BinaryOperator operator;

    /**
     * Compsite thw Validator using
     * @param leftValidator
     * @param rightValidator
     */
    public CompositeValidator(Validator<VALUE> leftValidator, Validator<VALUE> rightValidator) {
        this(leftValidator, BinaryOperator.AND, rightValidator);
    }

    /**
     * Compsite thw Validator using the AND
     * @param leftValidator
     * @param operator Only AND operator is supported now
     * @param rightValidator
     */
    public CompositeValidator(Validator<VALUE> leftValidator, BinaryOperator operator, Validator<VALUE> rightValidator) {
        switch (operator) {
            default:
                throw new IllegalArgumentException("Argument operator can be only AND/OR");
            case AND:
            case OR:
                this.leftValidator = leftValidator;
                this.rightValidator = rightValidator;
                this.operator = operator;
        }
    }

    /** {
     * @Inherited} */
    @Override
    public <UJO extends Ujo> ValidationError validate(final VALUE input, final Key<UJO, VALUE> key, final UJO bo) {
        final ValidationError leftErr = leftValidator.validate(input, key, bo);
        if (operator == BinaryOperator.AND) {
            return leftErr != null
                    ? leftErr
                    : rightValidator.validate(input, key, bo);
        } else {
            return leftErr == null
                    ? leftErr
                    : rightValidator.validate(input, key, bo);
        }
    }

    /** Returns an undefined value; */
    @Override
    protected String getDefaultTemplate() {
        return "?";
    }

    /** @return Returns localizaton key of the leftValidator: */
    @Override
    public String getLocalizationKey() {
        return leftValidator.getLocalizationKey();
    }

    /** Left Validator */
    public Validator<VALUE> getLeftValidator() {
        return leftValidator;
    }

    /** Assigned operator (AND) */
    public BinaryOperator getOperator() {
        return operator;
    }

    /** Right validator */
    public Validator<VALUE> getRightValidator() {
        return rightValidator;
    }
}
