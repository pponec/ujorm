package org.ujorm.orm.demo;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

@Getter @Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "employee")
public class Employee extends UserSnapshotable<Employee> {

    @Column(name = "contract_day", nullable = false)
    private LocalDate contractDay;

    @Nullable
    @JoinColumn(name = "superior_id")
    private UserSnapshotable superior;

    /** Non-peristent attribute */
    @Transient
    private String nonPersistentA;

    /** Non-peristent attribute */
    transient private String nonPersistentB;

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
