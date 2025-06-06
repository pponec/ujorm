/*
 *  Copyright 2018-2022 Pavel Ponec
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
package org.ujorm.core.enums;

/**
 * Common attribues for many methods.
 * @author Pavel Ponec
 * @see org.ujorm.tools.Check#firstItem(java.lang.Object, java.lang.Object...)
 */
public enum OptionEnum {

    /** Most methods with this property return a <strong>non null</strong> object,
     * otherwise it throws an <strong>exception</strong>.
     * See the usage method for more information.
     */
    REQUIRED,
    /** A result of the most methods can be a <strong>nullable</strong> value.
     * See the usage method for more information.
     */
    OPTIONAL,
    /** The choice is an equivalence empty value,
     * the default behavior is described on the used method.
     */
    DEFAULT

}
