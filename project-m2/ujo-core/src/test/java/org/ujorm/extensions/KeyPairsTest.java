/*
 *  Copyright 2007-2022 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.extensions;

import junit.framework.TestCase;
import org.ujorm.implementation.universe.UniUjoBase;

/**
 * KeyPairsTest
 * @author Pavel Ponec
 */
public class KeyPairsTest extends org.junit.jupiter.api.Assertions {

    /**
     * Test of copyToTarget method, of class KeyPairList.
     */
    public void testCopyToTarget() {
        System.out.println("copyToTarget");

        UniUjoBase p1 = new UniUjoBase();
        UniUjoBase p2 = new UniUjoBase();

        KeyPairs<UniUjoBase,UniUjoBase> pairs = KeyPairs.get();

        pairs.add(UniUjoBase.PRO_P0, UniUjoBase.PRO_P0);
        pairs.add(UniUjoBase.PRO_P1, UniUjoBase.PRO_P1);
        pairs.add(UniUjoBase.PRO_P2, UniUjoBase.PRO_P2);

        UniUjoBase.PRO_P0.setValue(p1, 300L);
        UniUjoBase.PRO_P1.setValue(p1, 9);
        pairs.copyToTarget(p1, p2);
        assertEquals(UniUjoBase.PRO_P0.of(p1), UniUjoBase.PRO_P0.of(p2));
        assertEquals(UniUjoBase.PRO_P1.of(p1), UniUjoBase.PRO_P1.of(p2));
    }


}
