package org.ujorm.core.demo;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.AbstractSnapshotable;

import java.time.LocalDate;

@Getter @Setter @ToString @EqualsAndHashCode(callSuper = false)
@Table(name = "employee")
public class Employee extends AbstractSnapshotable<Employee> {

    @Column(name = "id") @Id
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Nullable
    @ManyToOne
    @JoinColumn(name = "boss_id")
    private Employee boss;
    @JoinColumn(name = "city", nullable = false)
    private City city;
    @Column(name = "contract_day", nullable = false)
    private LocalDate contractDay;
    @Column(name = "is_active")
    private boolean active;

}
