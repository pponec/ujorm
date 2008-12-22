/*
 *  Copyright 2007-2008 Paul Ponec
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
   

package org.ujoframework.tools.criteria;

import org.ujoframework.Ujo;

/**
 * The expression value is independent on the UJO object.
 * @author pavel
 */
final public class ExpressionIndependent<UJO extends Ujo> implements Expression<UJO> {
    
    private final boolean result;
    
    ExpressionIndependent(final boolean result) {
        this.result = result;
    }

    /** Return the value from constructor always. */
    public boolean evaluate(UJO ujo) {
        return result;
    }

    public Expression<UJO> join(OperatorBinary operator, Expression<UJO> expr) {
        return new ExpressionBinary<UJO>(this, operator, expr);
    }

    @Override
    public String toString() {
        return String.valueOf(result);
    }
    
}
