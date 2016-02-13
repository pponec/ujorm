/*
 *  Copyright 2009-2014 Pavel Ponec
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

package org.ujorm.orm;

import java.io.Serializable;

/**
 * A Unique key of the entity OrmUjo
 * @author Pavel Ponec
 */
public class ForeignKey implements Serializable {

    /** There is strongly recommended that all serializable classes explicitly declare serialVersionUID value */
    private static final long serialVersionUID = 464564L;

    private final Object value;

    public ForeignKey(Object fk) {
        this.value = fk;
    }

    public Object getValue() {
        return value;
    }

    /** Returns a String of the value only */
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ForeignKey
            && value.equals(((ForeignKey)obj).value );
    }

    @Override
    public int hashCode() {
        final int result = 29 * 5
            + (this.value != null
            ? this.value.hashCode()
            : 0)
            ;
        return result;
    }

}
