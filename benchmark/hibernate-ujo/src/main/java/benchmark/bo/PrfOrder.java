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
import org.ujoframework.UjoProperty;
import org.ujoframework.UjoPropertyList;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.quick.QuickUjo;

/**
 * The Order
 * @author Ponec
 */
public class PrfOrder extends QuickUjo {

    public static final Property<PrfOrder,Long> id = newProperty(Long.class);
    public static final Property<PrfOrder,Boolean> deleted = newProperty(false);
    public static final Property<PrfOrder,Date> dateDeleted = newProperty(Date.class);
    public static final Property<PrfOrder,String> deletionReason = newProperty(String.class);
    public static final Property<PrfOrder,Boolean> paid = newProperty(Boolean.class);
    public static final Property<PrfOrder,String> publicId = newProperty(String.class);
    public static final Property<PrfOrder,Date> dateOfOrder = newProperty(Date.class);
    public static final Property<PrfOrder,String> paymentType = newProperty(String.class);
    public static final Property<PrfOrder,BigDecimal> discount = newProperty(BigDecimal.class);
    public static final Property<PrfOrder,String> orderType = newProperty(String.class);
    public static final Property<PrfOrder,String> language = newProperty(String.class);
    public static final Property<PrfOrder,PrfOrder> parent = newProperty(PrfOrder.class);
    public static final UjoProperty <PrfOrder,PrfUser>user = newProperty(PrfUser.class);

    // Optional code for better performance when creating instance:
    private static UjoPropertyList properties = init(PrfOrder.class);
    @Override public UjoPropertyList readProperties() { return properties; }


    // Setters and Getters:
    public Date getDateDeleted() {
        return dateDeleted.of(this); // Note: the of() method is an alias for getValue()
    }

    public void setDateDeleted(Date aDateDeleted) {
        dateDeleted.setValue(this, aDateDeleted);
    }

    public Date getDateOfOrder() {
        return dateOfOrder.of(this);
    }

    public void setDateOfOrder(Date aDateOfOrder) {
        dateOfOrder.setValue(this, aDateOfOrder);
    }

    public boolean isDeleted() {
        return deleted.of(this);
    }

    public void setDeleted(boolean aDeleted) {
        deleted.setValue(this, aDeleted);
    }

    public String getDeletionReason() {
        return deletionReason.of(this);
    }

    public void setDeletionReason(String aDeletionReason) {
        deletionReason.setValue(this, aDeletionReason);
    }

    public BigDecimal getDiscount() {
        return discount.of(this);
    }

    public void setDiscount(BigDecimal aDiscount) {
        discount.setValue(this, aDiscount);
    }

    public Long getId() {
        return id.of(this);
    }

    public void setId(Long anId) {
        id.setValue(this, anId);
    }

    public String getLanguage() {
        return language.of(this);
    }

    public void setLanguage(String aLanguage) {
        language.setValue(this, aLanguage);
    }

    public String getOrderType() {
        return orderType.of(this);
    }

    public void setOrderType(String anOrderType) {
        orderType.setValue(this, anOrderType);
    }

    public boolean isPaid() {
        return paid.of(this);
    }

    public void setPaid(boolean aPaid) {
        paid.setValue(this, aPaid);
    }

    public PrfOrder getParent() {
        return parent.of(this);
    }

    public void setParent(PrfOrder aParent) {
        parent.setValue(this, aParent);
    }

    public String getPaymentType() {
        return paymentType.of(this);
    }

    public void setPaymentType(String aPaymentType) {
        paymentType.setValue(this, aPaymentType);
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
