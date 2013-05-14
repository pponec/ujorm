/*
 * It can be a generated class in a future.
 * License the Apache License, Version 2.0,
 * Author: Pavel Ponec
 */

package org.ujorm.hotels.bo;

import java.math.BigDecimal;
import java.util.Date;
import org.ujorm.Key;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Comment;
import org.ujorm.core.KeyFactory;
import static org.ujorm.Validator.Build.*;

/** Hotel */
abstract public class Hotel extends OrmTable<Hotel> {

    private static final String UNIQUE_HOTEL_NAME="idx_hotel_name";
    
    /** Factory */
    private static final KeyFactory<Hotel> f = newFactory(Hotel.class);

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Hotel, Long> ID = f.newKey();
    /** Hotel state, default is ACTIVE (the true or null is required) */
    @Comment("Hotel state (the true or null is required)")
    public static final Key<Hotel, Boolean> ACTIVE = f.newKey(required(true));
    /** Description of the Company */
    @Comment("Name of the Hotel")
    @Column(index=UNIQUE_HOTEL_NAME)
    public static final Key<Hotel, String> NAME = f.newKey(length(MANDATORY, 3, 80));
    /** Price per night in EUR */
    @Comment("Price per night in EUR")
    public static final Key<Hotel, BigDecimal> PRICE = f.newKey(min(MANDATORY, BigDecimal.ZERO));
    /** Description of the Company */
    @Comment("Description of the hotel")
    public static final Key<Hotel, String> NOTE = f.newKey(length(256));
    /** Date of creation */
    @Comment("Date of creation")
    public static final Key<Hotel, Date> CREATED = f.newKey(mandatory());
    
    static {
        f.lock();
    }    

    // --- Getters / Setters ---

    //<editor-fold defaultstate="collapsed" desc="Setters and Getters generated from NetBeans">
    
    /** The Primary Key */
    public Long getId() {
        return ID.of(this);
    }
    
    /** The Primary Key */
    public void setId(Long id) {
        Hotel.ID.setValue(this, id);
    }
    
    /** Hotel state, default is ACTIVE (the true or null is required) */
    public Boolean getActive() {
        return ACTIVE.of(this);
    }
    
    /** Hotel state, default is ACTIVE (the true or null is required) */
    public void setActive(Boolean active) {
        Hotel.ACTIVE.setValue(this, active);
    }
    
    /** Description of the Company */
    public String getName() {
        return NAME.of(this);
    }
    
    /** Description of the Company */
    public void setName(String name) {
        Hotel.NAME.setValue(this, name);
    }
    
    /** Price per night in EUR */
    public BigDecimal getPrice() {
        return PRICE.of(this);
    }
    
    /** Price per night in EUR */
    public void setPrice(BigDecimal price) {
        Hotel.PRICE.setValue(this, price);
    }
    
    /** Description of the Company */
    public String getNote() {
        return NOTE.of(this);
    }
    
    /** Description of the Company */
    public void setNote(String note) {
        Hotel.NOTE.setValue(this, note);
    }
    
    /** Date of creation */
    public Date getCreated() {
        return CREATED.of(this);
    }
    
    /** Date of creation */
    public void setCreated(Date created) {
        Hotel.CREATED.setValue(this, created);
    }
    //</editor-fold>
    


}
