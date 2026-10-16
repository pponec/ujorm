package org.ujorm.orm.converter;

/** E-mail value object stored in a single column */
public record Email(String value) {
    public Email {
        if (value == null || !value.contains("@")) {
            throw new IllegalArgumentException("Invalid e-mail: " + value);
        }
        value = value.toLowerCase();
    }
}
