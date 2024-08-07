/*
 * Copyright 2021-2022 Pavel Ponec
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

package org.ujorm.tools.common;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for common export an object to an appendable writer
 * 
 * @author Pavel Ponec
 */
@FunctionalInterface
public interface Confessionable {
    
    /**
     * Confess data to a writer
     * @param writer An output sequence
     * @return Return the argument object.
     */
    @NotNull
    Appendable confessTo(@NotNull Appendable writer) throws IOException;
    
}
