/*
 * Copyright 2019-2026 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/JdbcBuilder.java
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Pavel Ponec
 */
public class SafelyEqualableTest {

    @Test
    public void testSomeMethod() {

        assertTrue (TestEnum.A.equalsSafely(TestEnum.A));
        assertFalse(TestEnum.B.equalsSafely(TestEnum.C));
        assertFalse(TestEnum.C.equalsSafely(null));

    }

}
