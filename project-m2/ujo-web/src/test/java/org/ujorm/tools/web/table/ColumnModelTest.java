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
public class ColumnModelTest {

    @Test
    public void testOf() {
        ColumnModel expected, result;

        expected = new ColumnModel(Direction.DOWN, 2);
        result = ColumnModel.ofCode(expected.toString());
        assertEquals(expected, result);        
        
        expected = new ColumnModel(Direction.UP, 1);
        result = ColumnModel.ofCode(expected.toString());
        assertEquals(expected, result);
        
        expected = new ColumnModel(Direction.DOWN, 0);
        result = ColumnModel.ofCode(expected.toString());
        assertEquals(expected, result);
         
        expected = new ColumnModel(Direction.UP, 0);
        result = ColumnModel.ofCode(expected.toString());
        assertEquals(expected, result);
    }
    
}
