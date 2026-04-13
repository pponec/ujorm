package org.ujorm.orm.dsl.meta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Represents the entity class type.
 * Note that the {@link Entity} annotation is not required in this library. */
@Table(name = "city")
public record City(
    @Column(name = "id") @Id
    Long id,
    @Column(name = "name", nullable = false)
    String name,
    @Column(name = "country_code", nullable = false)
    String countryCode
) {

}
