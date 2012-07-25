/*
 *  Copyright 2009 Paul Ponec
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
import org.ujorm.implementation.quick.QuickUjo;

/**
 * The Order Item
 * @author Ponec
 */
public class PrfOrderItem extends QuickUjo {

    public static final Key<PrfOrderItem,Long> id = newKey();
    public static final Key<PrfOrderItem,String> publicId = newKey();
    public static final Key<PrfOrderItem,Boolean> deleted = newKey(false);
    public static final Key<PrfOrderItem,Date> dateDeleted = newKey();
    public static final Key<PrfOrderItem,BigDecimal> price = newKey();
    public static final Key<PrfOrderItem,BigDecimal> charge = newKey();
    public static final Key<PrfOrderItem,Boolean> arrival = newKey();
    public static final Key<PrfOrderItem,String> description = newKey();
    public static final Key<PrfOrderItem,PrfUser> user = newKey();
    public static final Key<PrfOrderItem,PrfOrder> order = newKey();
    public static final Key<PrfOrderItem,PrfOrderItem> parent = newKey();

    // Optional code for better performance when creating instance:
    private static KeyList properties = init(PrfOrderItem.class);
    @Override public KeyList readKeys() { return properties; }


    // Setters and Getters:
    public boolean isArrival() {
        return arrival.of(this);
    }

    public void setArrival(boolean anArrival) {
        arrival.setValue(this, anArrival);
    }

    public BigDecimal getCharge() {
        return charge.of(this);
    }

    public void setCharge(BigDecimal aCharge) {
        charge.setValue(this, aCharge);
    }

    public Date getDateDeleted() {
        return dateDeleted.of(this);
    }

    public void setDateDeleted(Date aDateDeleted) {
        dateDeleted.setValue(this, aDateDeleted);
    }

    public boolean isDeleted() {
        return deleted.of(this);
    }

    public void setDeleted(boolean aDeleted) {
        deleted.setValue(this, aDeleted);
    }

    public String getDescription() {
        return description.of(this);
    }

    public void setDescription(String aDescription) {
        description.setValue(this, aDescription);
    }

    public Long getId() {
        return id.of(this);
    }

    public void setId(Long anId) {
        id.setValue(this, anId);
    }

    public PrfOrder getOrder() {
        return order.of(this);
    }

    public void setOrder(PrfOrder anOrder) {
        order.setValue(this, anOrder);
    }

    public PrfOrderItem getParent() {
        return parent.of(this);
    }

    public void setParent(PrfOrderItem aParent) {
        parent.setValue(this, aParent);
    }

    public BigDecimal getPrice() {
        return price.of(this);
    }

    public void setPrice(BigDecimal aPrice) {
        price.setValue(this, aPrice);
    }

    public String getPublicId() {
        return publicId.of(this);
    }

    public void setPublicId(String aPublicId) {
        publicId.setValue(this, aPublicId);
    }

    public PrfUser getUser() {
        return user.of(this);
    }

    public void setUser(PrfUser anUser) {
        user.setValue(this, anUser);
    }

}
