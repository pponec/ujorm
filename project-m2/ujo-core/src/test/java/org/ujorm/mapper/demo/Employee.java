package org.ujorm.mapper.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

@Getter @Setter @ToString @EqualsAndHashCode
@Table(name = "employee")
public class Employee {

    @Column(name = "id") @Id
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Nullable
    @JoinColumn(name = "superior_id")
    private Employee superior;
    @JoinColumn(name = "city", nullable = false)
    private City city;
    @Column(name = "contract_day", nullable = false)
    private LocalDate contractDay;
    @Column(name = "is_active")
    private boolean active;

}
