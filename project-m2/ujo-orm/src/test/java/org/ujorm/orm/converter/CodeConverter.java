package org.ujorm.orm.converter;

import jakarta.persistence.AttributeConverter;

/** A generic base of the Enum ⇄ code converters, the type arguments are resolved by a subclass. */
public abstract class CodeConverter<E extends Enum<E> & CodeConverter.Coded> implements AttributeConverter<E, String> {

    /** Enum with a database code */
    public interface Coded {
        String code();
    }

    private final E[] items;

    protected CodeConverter(Class<E> type) {
        this.items = type.getEnumConstants();
    }

    @Override
    public String convertToDatabaseColumn(E value) {
        return value.code();
    }

    @Override
    public E convertToEntityAttribute(String dbValue) {
        for (var item : items) {
            if (item.code().equals(dbValue)) {
                return item;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + dbValue);
    }
}
