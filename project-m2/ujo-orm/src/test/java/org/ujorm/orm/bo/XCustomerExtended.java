/*
 *  Copyright 2018-2022 Pavel Ponec
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
package org.ujorm.orm.bo;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.orm.annot.Table;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the XOrder object has got an collection of Items.
 * @hidden
 */
@Table(name = "ord_order_extended")
public class XCustomerExtended extends XCustomer {

    private static final KeyFactory<XCustomerExtended> f = newCamelFactory(XCustomerExtended.class);

    /** Extended key */
    public static final Key<XCustomerExtended, Long> ID_EXTENDED = f.newKeyDefault(202L);

    static {
        f.lock();
    }

    // --- Generated setters and getters ---

    /** Extended key */
    public Long getIdExtended() {
        return ID_EXTENDED.of(this);
    }

    /** Extended key */
    public void setIdExtended(Long idExtended) {
        ID_EXTENDED.setValue(this, idExtended);
    }

}
