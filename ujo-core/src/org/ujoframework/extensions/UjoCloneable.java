/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.extensions;

/**
 * Interface adds a clone facility. Note the interface does not extends a Cloneable. 
 * If you need so, implements the Cloneable explicitly.<br>
 * @see org.ujoframework.core.UjoManager#clone(org.ujoframework.Ujo, int, Object)
 * @author Pavel Ponec
 */
public interface UjoCloneable /*extends Ujo, Cloneable */ {
    
    /**
     * Object is Cloneable
     * @param depth Depth of clone. <br>Sample: value "0" returns the same object, value "1" returns the same attribute values, etc.
     * @param context Context of the action. A default value can be a NULL.
     * @return A clone
     */
    public Object clone(int depth, Object context);
    
    
}
