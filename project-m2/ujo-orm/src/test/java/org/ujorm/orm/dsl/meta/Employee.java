package org.ujorm.orm.dsl.meta;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
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

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "state_ordinal", nullable = false)
    private EmployeeState stateOrdinal = EmployeeState.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_string", nullable = false)
    private EmployeeState stateString = EmployeeState.ACTIVE;

    /** Create new instance without ID */
    public static Employee of(String name, City city, @Nullable Employee boss) {
        var result = new Employee();
        result.setName(name);
        result.setCity(city);
        result.setBoss(boss);
        result.setStateOrdinal(EmployeeState.ACTIVE);
        result.setStateString(EmployeeState.ACTIVE);
        return result;
    }

}
