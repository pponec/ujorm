package org.ujorm.doman;

import java.math.BigDecimal;

/**
 *
 * @author Pavel Ponec
 */
public class Item {
    
    /** Unique key */
    private Integer id;
    private String note;
    private BigDecimal price;
    private Order order;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
    

    
}
