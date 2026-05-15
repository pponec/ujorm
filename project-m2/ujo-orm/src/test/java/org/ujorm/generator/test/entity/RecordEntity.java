package org.ujorm.generator.test.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/** Record entity mapping */
@Table(name = "record_entity")
public record RecordEntity(
        int id,
        boolean active,
        String name,
        @Transient String ignoredTransient
) {
}