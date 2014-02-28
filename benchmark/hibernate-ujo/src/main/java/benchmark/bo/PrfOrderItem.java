/*
 *  Copyright 2009 Pavel Ponec
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

package benchmark.bo;

import java.math.BigDecimal;
import java.util.Date;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.quick.QuickUjo;

/**
 * The Order Item
 * @author Ponec
 */
public class PrfOrderItem extends QuickUjo {
    private static final KeyFactory<PrfOrderItem> f = newCamelFactory(PrfOrderItem.class);

    public static final Key<PrfOrderItem,Long> ID = f.newKey();
    public static final Key<PrfOrderItem,String> PUBLIC_ID = f.newKey();
    public static final Key<PrfOrderItem,Boolean> DELETED = f.newKeyDefault(false);
    public static final Key<PrfOrderItem,Date> DATE_DELETED = f.newKey();
    public static final Key<PrfOrderItem,BigDecimal> PRICE = f.newKey();
    public static final Key<PrfOrderItem,BigDecimal> CHARGE = f.newKey();
    public static final Key<PrfOrderItem,Boolean> ARRIVAL = f.newKey();
    public static final Key<PrfOrderItem,String> DESCRIPTION = f.newKey();
    public static final Key<PrfOrderItem,PrfUser> USER = f.newKey();
    public static final Key<PrfOrderItem,PrfOrder> ORDER = f.newKey();
    public static final Key<PrfOrderItem,PrfOrderItem> PARENT = f.newKey();

    // Optional code for better performance when creating instance:
    static { f.lock(); }

    @Override
    public KeyList<PrfOrderItem> readKeys() { return f.getKeys(); }

    // Setters and Getters:

    public Long getId() {
        return ID.of(this);
    }

    public void setId(Long id) {
        PrfOrderItem.ID.setValue(this, id);
    }

    public String getPublicId() {
        return PUBLIC_ID.of(this);
    }

    public void setPublicId(String publicId) {
        PrfOrderItem.PUBLIC_ID.setValue(this, publicId);
    }

    public Boolean getDeleted() {
        return DELETED.of(this);
    }

    public void setDeleted(Boolean deleted) {
        PrfOrderItem.DELETED.setValue(this, deleted);
    }

    public Date getDateDeleted() {
        return DATE_DELETED.of(this);
    }

    public void setDateDeleted(Date dateDeleted) {
        PrfOrderItem.DATE_DELETED.setValue(this, dateDeleted);
    }

    public BigDecimal getPrice() {
        return PRICE.of(this);
    }

    public void setPrice(BigDecimal price) {
        PrfOrderItem.PRICE.setValue(this, price);
    }

    public BigDecimal getCharge() {
        return CHARGE.of(this);
    }

    public void setCharge(BigDecimal charge) {
        PrfOrderItem.CHARGE.setValue(this, charge);
    }

    public Boolean getArrival() {
        return ARRIVAL.of(this);
    }

    public void setArrival(Boolean arrival) {
        PrfOrderItem.ARRIVAL.setValue(this, arrival);
    }

    public String getDescription() {
        return DESCRIPTION.of(this);
    }

    public void setDescription(String description) {
        PrfOrderItem.DESCRIPTION.setValue(this, description);
    }

    public PrfUser getUser() {
        return USER.of(this);
    }

    public void setUser(PrfUser user) {
        PrfOrderItem.USER.setValue(this, user);
    }

    public PrfOrder getOrder() {
        return ORDER.of(this);
    }

    public void setOrder(PrfOrder order) {
        PrfOrderItem.ORDER.setValue(this, order);
    }

    public PrfOrderItem getParent() {
        return PARENT.of(this);
    }

    public void setParent(PrfOrderItem parent) {
        PrfOrderItem.PARENT.setValue(this, parent);
    }

}
