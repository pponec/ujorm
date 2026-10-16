package org.ujorm.orm.converter;

import jakarta.persistence.AttributeConverter;

/** Boolean ⇄ 'Y' / 'N' */
public class YesNoConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean value) {
        return value ? "Y" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String dbValue) {
        return "Y".equals(dbValue);
    }
}
