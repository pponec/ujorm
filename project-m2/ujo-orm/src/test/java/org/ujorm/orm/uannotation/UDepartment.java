package org.ujorm.orm.uannotation;

import lombok.Getter;
import lombok.Setter;
import org.ujorm.annotation.UColumn;
import org.ujorm.annotation.UId;
import org.ujorm.annotation.UTable;

/** A department mapped by the Ujorm annotations only */
@Getter @Setter
@UTable(name = "u_department")
public class UDepartment {

    @UId
    private Long id;

    @UColumn(name = "dep_name", nullable = false)
    private String name;

    public static UDepartment of(String name) {
        var result = new UDepartment();
        result.setName(name);
        return result;
    }
}
