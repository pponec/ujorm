/*
 * Copyright 2021-2021 Pavel Ponec, https://github.com/pponec
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
public class SortedColumnTest {
   

    @Test
    public void testOf() {
        SortedColumn expected, result;

        expected = new SortedColumn(true, 2);
        result = SortedColumn.of(expected.toString());
        assertEquals(expected, result);        
        
        expected = new SortedColumn(false, 1);
        result = SortedColumn.of(expected.toString());
        assertEquals(expected, result);
        
        expected = new SortedColumn(true, 0);
        result = SortedColumn.of(expected.toString());
        assertEquals(expected, result);
         
        expected = new SortedColumn(false, 0);
        result = SortedColumn.of(expected.toString());
        assertEquals(expected, result);
                
    }
    
}
