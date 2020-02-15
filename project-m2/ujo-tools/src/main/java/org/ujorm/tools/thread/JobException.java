/*
 * Copyright 2020-2020 Pavel Ponec
 * Original source of Ujorm framework: https://bit.ly/340mx4T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.tools.thread;

import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;

/**
 *
 * @author Pavel Ponec
 */
public class JobException extends IllegalStateException {

    public JobException(@Nonnull String message) {
        super(Assert.hasLength(message, "message"));
    }

    public JobException(Throwable cause) {
        super(Assert.notNull(cause, Jobs.REQUIRED_INPUT_TEMPLATE_MSG, "cause"));
    }
}
