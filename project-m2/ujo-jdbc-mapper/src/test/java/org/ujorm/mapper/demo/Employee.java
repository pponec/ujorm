package org.ujorm.mapper.demo;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

@Getter @Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "employee")
public class Employee extends User {

    @Column(name = "contract_day", nullable = false)
    private LocalDate contractDay;

    @Nullable
    @JoinColumn(name = "superior_id")
    private User superior;

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

}
