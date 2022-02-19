/*
 *  Copyright 2012-2022 Pavel Ponec
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

package org;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ujorm.WeakKey;
import org.ujorm.core.WeakKeyFactory;

/**
 * WeakKey sample Service
 * @author ponec
 */
public class SampleWeakKeyService {
    private static final WeakKeyFactory f = new WeakKeyFactory(SampleWeakKeyService.class);

    public static final WeakKey<String>     NAME = f.newKey();
    public static final WeakKey<Date>       BORN = f.newKey();
    public static final WeakKey<Boolean>    WIFE = f.newKeyDefault(Boolean.TRUE);
    public static final WeakKey<BigDecimal> CASH = f.newKeyDefault(BigDecimal.ZERO);

    static {
        f.lock(); // Initialize all keys and lock them.
    }

    /** Sample how to use weak keys with a List. */
    public void testWeakKeys2List() {
        List<Object> list = new ArrayList<Object>();

        assert NAME.of(list) == null;
        assert BORN.of(list) == null;
        assert WIFE.of(list) == Boolean.TRUE;
        assert CASH.of(list) == BigDecimal.ZERO;

        final String name = "Lucy";
        final Boolean wife = true;
        final Date today = new Date();
        final BigDecimal cash = BigDecimal.TEN;

        NAME.setValue(list, name);
        BORN.setValue(list, today);
        WIFE.setValue(list, wife);
        CASH.setValue(list, cash);

        assert NAME.of(list).equals(name);
        assert BORN.of(list).equals(today);
        assert WIFE.of(list).equals(wife);
        assert CASH.of(list).equals(cash);
    }

    /** Similar sample how to use weak keys with a Map. */
    public void testWeakKeys2Map() {
        Map<String,Object> map = new HashMap<String, Object>();

        assert NAME.of(map) == null;
        assert BORN.of(map) == null;
        assert WIFE.of(map) == Boolean.TRUE;
        assert CASH.of(map) == BigDecimal.ZERO;

        final String name = "Lucy";
        final Boolean wife = true;
        final Date today = new Date();
        final BigDecimal cash = BigDecimal.TEN;

        NAME.setValue(map, name);
        BORN.setValue(map, today);
        WIFE.setValue(map, wife);
        CASH.setValue(map, cash);

        assert NAME.of(map).equals(name);
        assert BORN.of(map).equals(today);
        assert WIFE.of(map).equals(wife);
        assert CASH.of(map).equals(cash);
    }

    /** Test key attributes */
    public void testWeakKeyAttributes() {
        assert NAME.getIndex()==0;
        assert BORN.getIndex()==1;
        assert WIFE.getIndex()==2;
        assert CASH.getIndex()==3;

        assert NAME.getName().equals("name");
        assert BORN.getName().equals("born");
        assert WIFE.getName().equals("wife");
        assert CASH.getName().equals("cash");

        assert NAME.isTypeOf(String.class);
        assert BORN.isTypeOf(Date.class);
        assert WIFE.isTypeOf(Boolean.class);
        assert CASH.isTypeOf(BigDecimal.class);
    }
}
