package org.ujorm.generator.test.entity;

import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/** Entity using Lombok for boilerplate code generation */
@Table(name = "lombok_outer")
@Getter
@Setter
public class LombokEntity {

    private int id;
    private boolean active;
    private String name;
    private transient String ignoredTransient;

    @Setter(AccessLevel.NONE)
    private String ignoredNoSetter;

    @Getter(AccessLevel.NONE)
    private String ignoredNoGetter;

    /** Inner static Lombok class */
    @Table(name = "lombok_inner")
    @Getter
    @Setter
    public static class InnerLombok {
        private int id;
        private boolean active;
        private String name;
        private transient String ignoredTransient;

        @Setter(AccessLevel.NONE)
        private String ignoredNoSetter;

        @Getter(AccessLevel.NONE)
        private String ignoredNoGetter;
    }
}