package org.ujorm.orm.uannotation;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.ujorm.annotation.*;
import org.ujorm.core.converter.DbConverter;

/** A worker mapped by the Ujorm annotations only, the table name comes from the class name */
@Getter @Setter
@UEntity
public class UWorker {

    @UId
    private Long id;

    @UColumn(nullable = false)
    private String name;

    @UManyToOne
    @UJoinColumn(name = "dep_id", nullable = false)
    private UDepartment department;

    /** The Ujorm converter needs no Jakarta Persistence API */
    @UConvert(converter = YesNoConverter.class)
    @UColumn(nullable = false)
    private Boolean active;

    @UEnumerated(UEnumType.STRING)
    private Level level;

    @UEnumerated
    private Level levelOrdinal;

    @UTransient
    private String note;

    public enum Level { JUNIOR, SENIOR }

    /** Maps a Boolean to the Y/N */
    public static class YesNoConverter implements DbConverter<Boolean, String> {
        @Override public @NotNull Class<String> dbType() { return String.class; }
        @Override public String toDb(@NotNull Boolean value) { return value ? "Y" : "N"; }
        @Override public Boolean fromDb(@NotNull String value) { return "Y".equals(value); }
        @Override public boolean equals(Object obj) { return obj != null && obj.getClass() == getClass(); }
        @Override public int hashCode() { return getClass().hashCode(); }
    }

    public static UWorker of(String name, UDepartment department, boolean active, Level level) {
        var result = new UWorker();
        result.setName(name);
        result.setDepartment(department);
        result.setActive(active);
        result.setLevel(level);
        result.setLevelOrdinal(level);
        result.setNote("transient");
        return result;
    }
}
