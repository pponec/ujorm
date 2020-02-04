package org.ujorm2.doman;

import java.math.BigDecimal;

/**
 * Order Item.
 * Domain object type of POJO
 * @author Pavel Ponec
 */
public class Item {

    /** Unique key */
    private Integer id;
    private String note;
    private BigDecimal price;
    private Order order;
    private Boolean descending;
    private Integer codePoints;

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

    public Boolean getDescending() {
        return descending;
    }

    public void setDescending(Boolean descending) {
        this.descending = descending;
    }

    public Integer getCodePoints() {
        return codePoints;
    }

    public void setCodePoints(Integer codePoints) {
        this.codePoints = codePoints;
    }

}
