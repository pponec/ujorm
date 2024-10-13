/*
 * Copyright 2018-2022 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlElement.java
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
package org.ujorm.tools.xml;

import java.io.Closeable;
import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.Assert;

/**
 * An element model API.
 *
 * The XmlElement class implements the {@link Closeable} implementation
 * for an optional highlighting the tree structure in the source code.
 *
 * @see org.ujorm.tools.web.HtmlElement
 * @since 1.86
 * @author Pavel Ponec
 * @deprecated Use the interface {@link ApiElement} rather.
 */
@Deprecated
public abstract class AbstractElement<E extends AbstractElement<?>> implements ApiElement<E> {

    @NotNull
    protected final String name;

    public AbstractElement(@NotNull final String name) {
        this.name = Assert.hasLength(name, "name");
    }

    @Override
    public String getName() {
        return name;
    }

}
