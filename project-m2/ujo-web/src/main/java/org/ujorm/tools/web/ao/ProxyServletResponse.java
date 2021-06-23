/*
 * Copyright 2020-2020 Pavel Ponec, https://github.com/pponec
 * https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/org/ujorm/ujoservlet/tools/Html.java
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
package org.ujorm.tools.web.ao;

/**
 * Simple proxy servlet response for getting a writer content
 * @author Pavel Ponec
 * @since 2.02
 * @Deprecated Use the {@link MockServletResponse} rather
 */
@Deprecated
public final class ProxyServletResponse extends MockServletResponse {

    public ProxyServletResponse() {
        super();
    }

    public ProxyServletResponse(int size) {
        super(size);
    }

}
