package org.ujorm.orm.uannotation;

import org.ujorm.annotation.*;
import org.ujorm.orm.converter.YesNoConverter;
import org.ujorm.orm.uannotation.UWorker.Level;

/**
 * A plain JavaBean mapped by the Ujorm annotations, which covers all their attributes.
 * The table lives in its own schema of the H2 database {@code TESTDB}.
 */
@UTable(name = "u_project", schema = "U_SCHEMA", catalog = "TESTDB")
public class UProject {

    /** The primary key */
    @UId
    private Long id;

    /** A renamed required column */
    @UColumn(name = "title", nullable = false)
    private String name;

    /** A column with the default name, the {@code NULL} value is allowed */
    @UColumn
    private String description;

    /** A required relation with a renamed column: the {@code INNER JOIN} */
    @UManyToOne
    @UJoinColumn(name = "owner_id", nullable = false)
    private UWorker owner;

    /** An optional relation by the join column only: the {@code LEFT JOIN} */
    @UJoinColumn(name = "dep_id")
    private UDepartment department;

    /** A foreign key kept as a raw value: a common column */
    @UJoinColumn(name = "parent_id")
    private Long parentId;

    /** The default Enum representation of the annotation: the ordinal */
    @UEnumerated
    private Level levelOrdinal;

    /** The Enum name */
    @UEnumerated(UEnumType.STRING)
    private Level level;

    /** A primitive type with the Ujorm {@code DbConverter} */
    @UConvert(converter = UWorker.YesNoConverter.class)
    private boolean active;

    /** A JPA {@code AttributeConverter} */
    @UConvert(converter = YesNoConverter.class)
    private Boolean archived;

    /** The conversion is disabled, so the Enum is stored by its default name */
    @UConvert(disableConversion = true)
    private Level levelPlain;

    /** Excluded by the annotation */
    @UTransient
    private String note;

    /** Excluded by the modifier */
    private transient String cache;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UWorker getOwner() { return owner; }
    public void setOwner(UWorker owner) { this.owner = owner; }
    public UDepartment getDepartment() { return department; }
    public void setDepartment(UDepartment department) { this.department = department; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Level getLevelOrdinal() { return levelOrdinal; }
    public void setLevelOrdinal(Level levelOrdinal) { this.levelOrdinal = levelOrdinal; }
    public Level getLevel() { return level; }
    public void setLevel(Level level) { this.level = level; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }
    public Level getLevelPlain() { return levelPlain; }
    public void setLevelPlain(Level levelPlain) { this.levelPlain = levelPlain; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getCache() { return cache; }
    public void setCache(String cache) { this.cache = cache; }

    public static UProject of(String name, UWorker owner, UDepartment department, Level level) {
        var result = new UProject();
        result.setName(name);
        result.setOwner(owner);
        result.setDepartment(department);
        result.setLevel(level);
        result.setLevelOrdinal(level);
        result.setLevelPlain(level);
        result.setActive(true);
        result.setArchived(false);
        result.setNote("note");
        result.setCache("cache");
        return result;
    }
}
