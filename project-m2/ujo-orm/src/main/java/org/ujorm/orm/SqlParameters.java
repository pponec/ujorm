/*
 *  Copyright 2010 Ponec.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ujorm.orm;

/**
 * The class for a special User parameters in the <strong>Native Query</strong>.
 * @see org.ujorm.orm.annot.View
 * @author Ponec
 */
public class SqlParameters {

    /** SQL parameter values */
    final private Object[] parameters;

    public SqlParameters(Object ... parameters) {
        this.parameters = parameters;
    }

    /**  SqlParametsrs */
    public Object[] getParameters() {
        return parameters;
    }

    /** Returns count of the SQL parameters */
    public int getCount() {
        return parameters.length;
    }

    /** Show all parameters. */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Object p : parameters) {
            if (sb.length()>0) {
                sb.append(", ");
            }
            sb.append(p);
        }
        return sb.toString();
    }

}
