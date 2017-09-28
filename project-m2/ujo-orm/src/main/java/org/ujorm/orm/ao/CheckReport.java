/*
 * Copyright 2010-2017 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.orm.ao;

/**
 * How to use the report of any check message?
 * @author Ponec
 * @see org.ujorm.orm.metaModel.MetaParams
 */
public enum CheckReport {

    /** Skip the check test. */
    SKIP,
    /** Skip the check test and Quote all SQL columns, tables and alias names.
     * <br>NOTE: The change of the parameter value affects the native SQL statements in Ujorm views.
     */
    QUOTE_SQL_NAMES,
    /** Log a WARNING with the conflict message. */
    WARNING,
    /** Throw an EXCEPTION with the conflict message.
     * This is the default option.
     */
    EXCEPTION;
}
