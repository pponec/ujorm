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

/**
 * An expression operators.
 * @author Ponec
 */
public enum Operator {
    /** Equals the value */
    EQ,
    /** Not equals the value */
    NOT_EQ,
    /** Great then the value */
    GT,
    /** Great or equals the value */
    GE,
    /** Less then the value */
    LT,
    /** Less or equals the value */
    LE,
    /** Regular expression */
    REGEXP,
    /** Not regular expression */
    NOT_REGEXP,
    /** Only for a CharSequence subtypes (include String) */
    EQUALS_CASE_INSENSITIVE,
    /** Only for a CharSequence subtypes (include String) */
    STARTS,
    /** Only for a CharSequence subtypes (include String) */
    STARTS_CASE_INSENSITIVE,
    /** Only for a CharSequence subtypes (include String) */
    ENDS,
    /** Only for a CharSequence subtypes (include String) */
    ENDS_CASE_INSENSITIVE,
    /** Only for a CharSequence subtypes (include String) */
    CONTAINS,
    /** Only for a CharSequence subtypes (include String) */
    CONTAINS_CASE_INSENSITIVE,;
}
