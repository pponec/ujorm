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
 * The Order
 * @author Ponec
 */
public class PrfOrder extends QuickUjo {
    private static final KeyFactory<PrfOrder> f = newCamelFactory(PrfOrder.class);

    public static final Key<PrfOrder,Long> ID = f.newKey();
    public static final Key<PrfOrder,Boolean> DELETED = f.newKeyDefault(false);
    public static final Key<PrfOrder,Date> DATE_DELETED = f.newKey();
    public static final Key<PrfOrder,String> DELETION_REASON = f.newKey();
    public static final Key<PrfOrder,Boolean> PAID = f.newKey();
    public static final Key<PrfOrder,String> PUBLIC_ID = f.newKey();
    public static final Key<PrfOrder,Date> DATE_OF_ORDER = f.newKey();
    public static final Key<PrfOrder,String> PAYMENT_TYPE = f.newKey();
    public static final Key<PrfOrder,BigDecimal> DISCOUNT = f.newKey();
    public static final Key<PrfOrder,String> ORDER_TYPE = f.newKey();
    public static final Key<PrfOrder,String> LANGUAGE = f.newKey();
    public static final Key<PrfOrder,PrfOrder> PARENT = f.newKey();
    public static final Key<PrfOrder,PrfUser> USER = f.newKey();

    // Optional code for better performance when creating instance:
    static { f.lock(); }

    @Override
    public KeyList<PrfOrder> readKeys() { return f.getKeys(); }


    // Setters and Getters:

    public Long getId() {
        return ID.of(this);
    }

    public void setId(Long id) {
        PrfOrder.ID.setValue(this, id);
    }

    public Boolean getDeleted() {
        return DELETED.of(this);
    }

    public void setDeleted(Boolean deleted) {
        PrfOrder.DELETED.setValue(this, deleted);
    }

    public Date getDateDeleted() {
        return DATE_DELETED.of(this);
    }

    public void setDateDeleted(Date dateDeleted) {
        PrfOrder.DATE_DELETED.setValue(this, dateDeleted);
    }

    public String getDeletionReason() {
        return DELETION_REASON.of(this);
    }

    public void setDeletionReason(String deletionReason) {
        PrfOrder.DELETION_REASON.setValue(this, deletionReason);
    }

    public Boolean getPaid() {
        return PAID.of(this);
    }

    public void setPaid(Boolean paid) {
        PrfOrder.PAID.setValue(this, paid);
    }

    public String getPublicId() {
        return PUBLIC_ID.of(this);
    }

    public void setPublicId(String publicId) {
        PrfOrder.PUBLIC_ID.setValue(this, publicId);
    }

    public Date getDateOfOrder() {
        return DATE_OF_ORDER.of(this);
    }

    public void setDateOfOrder(Date dateOfOrder) {
        PrfOrder.DATE_OF_ORDER.setValue(this, dateOfOrder);
    }

    public String getPaymentType() {
        return PAYMENT_TYPE.of(this);
    }

    public void setPaymentType(String paymentType) {
        PrfOrder.PAYMENT_TYPE.setValue(this, paymentType);
    }

    public BigDecimal getDiscount() {
        return DISCOUNT.of(this);
    }

    public void setDiscount(BigDecimal discount) {
        PrfOrder.DISCOUNT.setValue(this, discount);
    }

    public String getOrderType() {
        return ORDER_TYPE.of(this);
    }

    public void setOrderType(String orderType) {
        PrfOrder.ORDER_TYPE.setValue(this, orderType);
    }

    public String getLanguage() {
        return LANGUAGE.of(this);
    }

    public void setLanguage(String language) {
        PrfOrder.LANGUAGE.setValue(this, language);
    }

    public PrfOrder getParent() {
        return PARENT.of(this);
    }

    public void setParent(PrfOrder parent) {
        PrfOrder.PARENT.setValue(this, parent);
    }

    public PrfUser getUser() {
        return USER.of(this);
    }

    public void setUser(PrfUser user) {
        PrfOrder.USER.setValue(this, user);
    }

}
