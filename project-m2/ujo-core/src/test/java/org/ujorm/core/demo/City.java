package org.ujorm.core.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "city")
public record City (
    @Column(name = "db_id") @Id
    Long id,
    @Column(name = "db_name", nullable = false)
    String name,
    @Column(nullable = false)
    String countryCode,
    double latitude,
    double longitude
) {
    public static City of(Long id, String name) {
        return new City(id, name, "", 0.0, 0.0);
    }

}
