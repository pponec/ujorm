package org.ujorm2.doman;

import java.time.LocalDateTime;

/**
 * Anonumous user.
 * Domain object type of POJO
 * @author Pavel Ponec
 */
public class Anonymous {

    /** Unique key */
    private Integer id;
    private Short pin;
    private LocalDateTime created;
    private Anonymous parent;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Short getPin() {
        return pin;
    }

    public void setPin(Short pin) {
        this.pin = pin;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime crated) {
        this.created = crated;
    }

    public Anonymous getParent() {
        return parent;
    }

    public void setParent(Anonymous parent) {
        this.parent = parent;
    }




}
