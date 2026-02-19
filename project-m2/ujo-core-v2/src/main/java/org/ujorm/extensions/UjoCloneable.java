/*
 *  Copyright 2007-2026 Pavel Ponec
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

package org.ujorm.extensions;

/**
 * Interface adds a clone facility. Note the interface does not extends a Cloneable.
 * If you need so, implements the Cloneable explicitly.<br>
 * @see org.ujorm.core.UjoManager#clone(org.ujorm.Ujo, int, Object)
 * @author Pavel Ponec
 */
public interface UjoCloneable<U extends Object> /*extends Ujo, Cloneable */ {

    /**
     * Object is Cloneable
     * @param depth Depth of clone. <br>Sample: value "0" returns the same object, value "1" returns the same attribute values, etc.
     * @param context Context of the action. A default value can be a NULL.
     * @return A clone
     */
    U clone(int depth, Object context);

}
