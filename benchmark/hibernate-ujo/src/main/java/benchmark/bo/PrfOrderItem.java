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
import org.ujoframework.UjoPropertyList;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.quick.QuickUjo;

/**
 * Order item
 * @author ponec
 */
public class PrfOrderItem extends QuickUjo {

    public static final Property<PrfOrderItem,Long> id = newProperty(Long.class);
    public static final Property<PrfOrderItem,String> publicId = newProperty(String.class);
    public static final Property<PrfOrderItem,Boolean> deleted = newProperty(false);
    public static final Property<PrfOrderItem,Date> dateDeleted = newProperty(Date.class);
    public static final Property<PrfOrderItem,BigDecimal> price = newProperty(BigDecimal.class);
    public static final Property<PrfOrderItem,BigDecimal> charge = newProperty(BigDecimal.class);
    public static final Property<PrfOrderItem,Boolean> arrival = newProperty(Boolean.class);
    public static final Property<PrfOrderItem,String> description = newProperty(String.class);
    public static final Property<PrfOrderItem,PrfUser> user = newProperty(PrfUser.class);
    public static final Property<PrfOrderItem,PrfOrder> order = newProperty(PrfOrder.class);
    public static final Property<PrfOrderItem,PrfOrderItem> parent = newProperty(PrfOrderItem.class);

    // Optional code for better performance:
    private static UjoPropertyList properties = init(PrfOrderItem.class);
    @Override public UjoPropertyList readProperties() { return properties; }


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
