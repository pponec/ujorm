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


/*
 * The last change: $Date: $
 * Release: $Revision: $
 */

package org.ujoframework.tools.criteria;

import org.ujoframework.Ujo;

/**
 * Binary operator
 * @author pavel
 */
public enum OperatorBinary implements AbstractOperator {
    /** (a AND b) */
    AND,
    /** (a OR b) */
    OR,
    /** (a XOR b) <br>Note: the SQL language may not support the operator. */
    XOR,
    /** NOT (a OR b) <br>Note: the SQL language may not support the operator. */
    NOR,
    /** NOT (a AND b) <br>Note: the SQL language may not support the operator. */
    NAND,
    /** (a == b) <br>Note: the SQL language may not support the operator. */
    EQ,
    /** (!a) */
    NOT,
    ;

    /** Join two expressions. */ 
    public <UJO extends Ujo> Expression<UJO> join(final Expression<UJO> a, final Expression<UJO> b) {
        return a.join(this, b);
    }

    /** The operator is the BINARY type (not a value one) */
    public final boolean isBinary() {
        return false;
    }

    /** Returns Enum */
    public final Enum getEnum() {
        return this;
    }
}
