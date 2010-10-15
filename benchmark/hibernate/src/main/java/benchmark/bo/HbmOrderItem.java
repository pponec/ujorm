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
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 * Rodič vešech objednávek
 * @author ponec
 */
@org.hibernate.annotations.Entity(dynamicUpdate=true)
@javax.persistence.Entity
@javax.persistence.Table(name="hbm_item")
public class HbmOrderItem implements Serializable {

    @Id @GeneratedValue(strategy=GenerationType.AUTO)
    protected Long id;
    @Column(length=8)
    private String publicId;
    private boolean deleted;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date dateDeleted;
    private BigDecimal price;
    private BigDecimal charge = BigDecimal.ZERO;
    @Column(length=128)
    private boolean arrival = false;
    private String description;
    @ManyToOne
    private HbmUser user;
    @ManyToOne
    private HbmOrder order;
    @ManyToOne
    private HbmOrderItem parent;

    public boolean isArrival() {
        return arrival;
    }

    public void setArrival(boolean arrival) {
        this.arrival = arrival;
    }

    public BigDecimal getCharge() {
        return charge;
    }

    public void setCharge(BigDecimal charge) {
        this.charge = charge;
    }

    public Date getDateDeleted() {
        return dateDeleted;
    }

    public void setDateDeleted(Date dateDeleted) {
        this.dateDeleted = dateDeleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HbmOrder getOrder() {
        return order;
    }

    public void setOrder(HbmOrder order) {
        this.order = order;
    }

    public HbmOrderItem getParent() {
        return parent;
    }

    public void setParent(HbmOrderItem parent) {
        this.parent = parent;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public HbmUser getUser() {
        return user;
    }

    public void setUser(HbmUser user) {
        this.user = user;
    }


}
