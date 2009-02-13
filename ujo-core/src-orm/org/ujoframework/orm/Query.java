/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.orm;

import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.tools.criteria.Expression;

/**
 *
 * @author pavel
 */
public class Query<UJO extends Ujo> {

    public Query(Class<UJO> aClass, Expression<UJO> expA) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void readOnly(boolean b) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <ITEM> void setParameter(UjoProperty<UJO,ITEM> property, ITEM value) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void sizeRequired(boolean b) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
