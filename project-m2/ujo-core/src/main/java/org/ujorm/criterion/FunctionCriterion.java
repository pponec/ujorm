/*
 *  Copyright 2007-2016 Pavel Ponec
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

package org.ujorm.criterion;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.tools.Assert;

/**
 * The criterion for a value function where a lambda expression is supported.
 * @since 0.90
 * @author Pavel Ponec
 */
public final class FunctionCriterion<U extends Ujo, T> extends ValueCriterion<U> {
    static final long serialVersionUID = 20140128L;

    /**
     * Common constructor 
     * @param key Key
     * @param operator Value operator
     * @param valueFunction An function for the value where the {@null} value is not supported in ORM. The class should be serialized.
     */
    public FunctionCriterion
        ( @Nonnull final Key<U, ? extends Object> key
        , @Nonnull final Operator operator
        , @Nonnull final Supplier<T> valueFunction) {
        super(key, operator, valueFunction);
        Assert.notNull(valueFunction, "Function is required");
        Assert.isFalse(operator == Operator.XFIXED, "Unsupported operator {}", operator);
    }

    /** Returns the right node of the parent */
    @Override @Nullable
    public final T getRightNode() {
        return ((Supplier<T>) super.value).get();
    }
    
    /** Test a value is an instance of CharSequence or a type Key is type of CharSequence.
     * If parameter is not valid than method throws Exception.
     */
    @Override
    protected void makeCharSequenceTest(Object value) throws IllegalArgumentException {
        Assert.isTrue(value instanceof Supplier, "Only {} is supported", Supplier.class);
    }

}
