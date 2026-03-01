package org.ujorm.mapper.demo;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Snapshotable;

import java.time.LocalDate;

@Getter @Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "employee")
public class Employee extends User implements Snapshotable<Employee> {

    @Column(name = "contract_day", nullable = false)
    private LocalDate contractDay;

    @Nullable
    @JoinColumn(name = "superior_id")
    private User superior;

    /** Non-peristent attribute */
    @Transient
    private String nonPersistentA;

    /** Non-peristent attribute */
    transient private String nonPersistentB;

    /** Non-peristent attribute */
    @Nullable @Getter(AccessLevel.NONE)
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

    /** Static builder */
    public static Employee of(Long id, String name, Employee superior, City city, LocalDate contractDay, boolean active) {
        var result = new Employee();
        result.setId(id);
        result.setName(name);
        result.setSuperior(superior);
        result.setCity(city);
        result.setContractDay(contractDay);
        result.setActive(active);
        return result;
    }

    /** Static builder */
    public static Employee of(Long id, String name, boolean active) {
        return of(id, name, null,City.of(10L, "XXX"), LocalDate.now(), active);
    }

}
