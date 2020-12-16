/*
 * Copyright 2020-2020 Pavel Ponec, https://github.com/pponec
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

import java.util.function.Function;
import org.ujorm.tools.web.Element;

/**
 *
 * @author Pavel Ponec
 */
public interface Renderer<T> extends Function<T, Object>, CharSequence {

    @Deprecated
    @Override
    public default Object apply(T t) {
        return "?";
    }

    @Override
    public default int length() {
        return 1;
    }

    @Override
    public default char charAt(int index) {
        return '?';
    }

    @Override
    public default CharSequence subSequence(int start, int end) {
        return "?".substring(start, end);
    }

    /** Write a custom content of the table cell
     *
     * @param parent An element of the table detail.
     * @param value Value to write.
     */
    public void write(Element parent, T value);

}
