package org.ujorm.orm.tutorial.domains;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter @Setter
@Table(name = "employee")
public class Employee {

    @Id @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Nullable
    @JoinColumn(name = "boss_id")
    private Employee boss;

    /** Create new instance without ID */
    public static Employee of(String name, City city, @Nullable Employee boss) {
        var result = new Employee();
        result.setName(name);
        result.setCity(city);
        result.setBoss(boss);
        return result;
    }

}
