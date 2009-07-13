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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;


/**
 * Rodič vešech objednávek
 * @author ponec
 */

//@org.hibernate.annotations.Entity(dynamicUpdate=true)
@javax.persistence.Entity()
public class PrfOrder implements Serializable {

    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private boolean deleted;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateDeleted;
    @Column(length=128)
    private String deletionReason;
    private boolean paid;
    @Column(length=8)
    private String publicId;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateOfOrder;
    @Column(length=2)
    private String paymentType;
    private BigDecimal discount;
    @Column(length=2)
    private String orderType;
    @Column(name="language_code", length=2)
    private String language;
    @OneToOne()
    private PrfOrder parent;
    @OneToOne
    private PrfUser user;

    public Date getDateDeleted() {
        return dateDeleted;
    }

    public void setDateDeleted(Date dateDeleted) {
        this.dateDeleted = dateDeleted;
    }

    public Date getDateOfOrder() {
        return dateOfOrder;
    }

    public void setDateOfOrder(Date dateOfOrder) {
        this.dateOfOrder = dateOfOrder;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getDeletionReason() {
        return deletionReason;
    }

    public void setDeletionReason(String deletionReason) {
        this.deletionReason = deletionReason;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public PrfOrder getParent() {
        return parent;
    }

    public void setParent(PrfOrder parent) {
        this.parent = parent;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public PrfUser getUser() {
        return user;
    }

    public void setUser(PrfUser user) {
        this.user = user;
    }



}
