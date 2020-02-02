package org.ujorm2.doman;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 * @author Pavel Ponec
 */
public class Order {

    /** Store the value like VARCHAR. */
    public enum State  {
        ACTIVE,
        DELETED;
    }

    private Integer id;
    private State state;
    private BigDecimal totalPrice;
    private User user;
    private String note;
    private LocalDateTime created;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }




}
