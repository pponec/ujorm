package org.ujorm.orm.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Phone ⇄ VARCHAR, applied to every attribute of the Phone type */
@Converter(autoApply = true)
public class PhoneConverter implements AttributeConverter<Phone, String> {

    @Override
    public String convertToDatabaseColumn(Phone phone) {
        return phone.value().replace(" ", "");
    }

    @Override
    public Phone convertToEntityAttribute(String dbValue) {
        return new Phone(dbValue);
    }
}
