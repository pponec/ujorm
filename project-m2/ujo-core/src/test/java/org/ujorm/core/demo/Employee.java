package org.ujorm.core.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Snapshotable;

import java.time.LocalDate;

@Getter @Setter @ToString @EqualsAndHashCode
@Table(name = "employee")
public class Employee implements Snapshotable<Employee>  {

    @Column(name = "id") @Id
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Nullable
    @JoinColumn(name = "boss_id")
    private Employee boss;
    @JoinColumn(name = "city", nullable = false)
    private City city;
    @Column(name = "contract_day", nullable = false)
    private LocalDate contractDay;
    @Column(name = "is_active")
    private boolean active;

    // --- Snapshot implementation ---

    @Nullable
    private transient Employee _snapshot;

    @Override
    public Employee saveSnapshot() throws IllegalStateException {
        _snapshot = clone();
        return this;
    }

    @Override
    public @Nullable Employee readSnapshot() {
        return _snapshot;
    }

    @Override
    public @NotNull Employee clone() throws IllegalStateException {
        try {
            return (Employee) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

}
