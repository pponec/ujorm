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
package org.ujorm.ujoservlet.ajax;

import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;

/**
 *
 * @author Pavel Ponec
 */
public class Message {

    private final String text;

    private final boolean error;

    public Message(String text, boolean error) {
        this.text = text;
        this.error = error;
    }

    public String getText() {
        return text;
    }

    public boolean isError() {
        return error;
    }

    public static Message of(@Nonnull final String text) {
        return new Message(Assert.hasLength(text, "text"), false);
    }

    public static Message of(@Nonnull Throwable e) {
        String text = String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
        return new Message(text, true);
    }

    @Override
    public String toString() {
        return getText();
    }
    
}
