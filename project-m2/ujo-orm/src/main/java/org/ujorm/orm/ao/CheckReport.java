/*
 * Copyright 2010-2022 Pavel Ponec, https://github.com/pponec
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

    /** No quoting, skip the keword check. */
    SKIP,
    /** Quote all SQL columns, tables, columns and skip any keword checking.
     * <br>NOTE: The change of the parameter value affects the native SQL statements in Ujorm views.
     */
    QUOTE_SQL_NAMES,
    /** Quote SQL keywords only, but a buiding the SQL statement can be a bit slower.
     * <br>NOTE: Consider that SQL keywords may vary across different databases and their versions.
     */
    @Deprecated
    QUOTE_ONLY_SQL_KEYWORDS,
    /** No quoting, but log a WARNING if any keyword is found. */
    WARNING,
    /** No quoting, but all names are compared to keywords on starting. If a keyword is found, the <strong>exception</strong> is throwed.
     * This is the default option. */
    EXCEPTION
}
