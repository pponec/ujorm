package org.ujorm.mapper.tutorial.domains;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

@Getter @Setter
@Table(name = "employee")
public class Employee {

    @Column(name = "id") @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Nullable
    @JoinColumn(name = "superior_id")
    private Employee superior;

}
