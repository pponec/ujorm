package org.ujorm.mapper.demo;

import jakarta.persistence.*;
import lombok.*;
import org.ujorm.core.AbstractSnapshotable;

/** Represents the entity class type.
 * Note that the {@link Entity} annotation is not required in this library. */
@Getter @Setter @ToString
@EqualsAndHashCode
public class UserSnapshotable<D> extends AbstractSnapshotable<D> {
    @Column(name = "id") @Id
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
    @Column(name = "is_active")
    private boolean active;
}
