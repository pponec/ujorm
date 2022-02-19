/*
 * Copyright 2021-2022 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlModel.java
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
package org.ujorm.tools.web.table;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Pavel Ponec
 */
public class ColumnModelTest {
    
    private static final boolean OPPSITE = true;
    private static final boolean NATURAL = !OPPSITE;

    @Test
    public void testToCode_boolean_Appendable() throws Exception {
        
        ColumnModel col = new ColumnModel(Direction.ASC, 0);
        assertEquals("1", col.toCode(NATURAL));
        assertEquals("-1", col.toCode(OPPSITE));
        
        col = new ColumnModel(Direction.DESC, 0);
        assertEquals("-1", col.toCode(NATURAL));
        assertEquals("1", col.toCode(OPPSITE));
        
        col = new ColumnModel(Direction.NONE, 0);
        assertEquals("-1", col.toCode(NATURAL));
        assertEquals("1", col.toCode(OPPSITE));
        
    }
}
