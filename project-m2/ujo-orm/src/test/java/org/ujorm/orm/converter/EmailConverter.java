package org.ujorm.orm.converter;

import jakarta.persistence.AttributeConverter;

/** Email ⇄ VARCHAR */
public class EmailConverter implements AttributeConverter<Email, String> {

    @Override
    public String convertToDatabaseColumn(Email email) {
        return email.value();
    }

    @Override
    public Email convertToEntityAttribute(String dbValue) {
        return new Email(dbValue);
    }
}
