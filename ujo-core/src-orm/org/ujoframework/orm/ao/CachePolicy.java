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


package org.ujoframework.orm.ao;

/**
 * CachePolicy is available inside a one session only. If you close the session then close database connection and clear cache.
 * @author Pavel Ponec
 */
public enum CachePolicy {

    /** No chage is inabled */
    NONE,
    /** CachePolicy is onable on relation many-to-one only.
     * @deprecated the attribute is not supported yet.
     */
    MANY_TO_ONE,
    ;

}
