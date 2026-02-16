package org.ujorm.mapper.generated.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "city")
public record City (
    @Column(name = "id") @Id
    Long id,
    @Column(name = "name")
    String name,
    @Column(name = "country_code")
    String countryCode,
    Double latitude,
    Double longitude
) {
    public static final City of(Long id, String name) {
        return new City(id, name, "", 0.0, 0.0);
    }

};
