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
package org.ujoframework.orm.renderers;

import org.ujoframework.orm.SqlRenderer;

/** H2 (http://www.h2database.com) */
@SuppressWarnings("unchecked")
public class H2Renderer extends SqlRenderer {

    /** Returns a default JDBC Driver */
    public String getJdbcUrl() {
        return "jdbc:h2:mem:";
    }

    /** Returns a JDBC Driver */
    public String getJdbcDriver() {
        return "org.h2.Driver";
    }

}

