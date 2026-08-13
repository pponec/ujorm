package org.ujorm.orm.core;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.utils.EntityContext;

/**
 * The ORM maps a Java Record or a mutable JavaBean only.
 * See the {@link EntityManager} constructor.
 * <p>A JUnit 5 test needs no {@code public} modifier, but this one hosts nested entities whose
 * metamodel is generated into the {@code org.ujorm.gen_.*} package. A nested public class of
 * a package-private outer class is unreachable from there, so the generated source code fails to
 * compile. Hence the modifier and the suppression of the "no public test class" rule.
 */
@SuppressWarnings("java:S5786")
public class EntityManagerContractTest {

    private static final EntityContext CTX = EntityContext.ofDefault();

    /** A bean property with a getter only cannot be read from a database. */
    @Test
    void beanWithReadOnlyPropertyIsRejected() {
        var result = Assertions.assertThrows(IllegalStateException.class,
                () -> CTX.entityManager(ReadOnlyEntity.class));

        Assertions.assertTrue(result.getMessage().contains("ReadOnlyEntity"), result.getMessage());
        Assertions.assertTrue(result.getMessage().contains("webRelease"), result.getMessage());
        Assertions.assertTrue(result.getMessage().contains("@Transient"), result.getMessage());
    }

    /** The same property excluded by the @Transient annotation is accepted. */
    @Test
    void beanWithTransientAnnotationIsAccepted() {
        Assertions.assertNotNull(CTX.entityManager(TransientEntity.class));
    }

    /** The same property excluded by the transient modifier is accepted too. */
    @Test
    void beanWithTransientModifierIsAccepted() {
        Assertions.assertNotNull(CTX.entityManager(TransientModifierEntity.class));
    }

    /** No record component has a setter, so the check must skip a record. */
    @Test
    void recordIsAccepted() {
        Assertions.assertNotNull(CTX.entityManager(RecordEntity.class));
    }

    /** A relation with a getter only is an own property, so the owner rejects it. */
    @Test
    void readOnlyRelationPropertyIsRejected() {
        var result = Assertions.assertThrows(IllegalStateException.class,
                () -> CTX.entityManager(ReadOnlyRelationEntity.class));

        Assertions.assertTrue(result.getMessage().contains("ReadOnlyRelationEntity"), result.getMessage());
        Assertions.assertTrue(result.getMessage().contains("target"), result.getMessage());
    }

    /**
     * The domain model traverses the class hierarchy, so an inherited property belongs to the own
     * properties of the entity and the check must reject it too.
     */
    @Test
    void inheritedReadOnlyPropertyIsRejected() {
        var result = Assertions.assertThrows(IllegalStateException.class,
                () -> CTX.entityManager(InheritedReadOnlyEntity.class));

        Assertions.assertTrue(result.getMessage().contains("InheritedReadOnlyEntity"), result.getMessage());
        Assertions.assertTrue(result.getMessage().contains("webRelease"), result.getMessage());
    }

    /**
     * The check covers the own properties only. A read-only property of the relation target
     * reports itself on the first row mapping, not here, because the target keeps its own contract.
     */
    @Test
    void relationToReadOnlyEntityIsAcceptedAtConstruction() {
        Assertions.assertNotNull(CTX.entityManager(RelationEntity.class));
    }

    @Table(name = "read_only_entity")
    public static class ReadOnlyEntity {
        @Id
        private Long id;
        @Column(name = "web_release")
        private boolean webRelease;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public boolean isWebRelease() {
            return webRelease;
        }
    }

    @Table(name = "transient_entity")
    public static class TransientEntity {
        @Id
        private Long id;
        @Transient
        private boolean webRelease;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public boolean isWebRelease() {
            return webRelease;
        }
    }

    @Table(name = "transient_modifier_entity")
    public static class TransientModifierEntity {
        @Id
        private Long id;
        private transient boolean webRelease;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public boolean isWebRelease() {
            return webRelease;
        }
    }

    @Table(name = "record_entity")
    public record RecordEntity(@Id Long id, String name) {}

    /** All own properties are writable, but the relation target has a read-only one. */
    @Table(name = "relation_entity")
    public static class RelationEntity {
        @Id
        private Long id;
        @JoinColumn(name = "read_only_id")
        private ReadOnlyEntity readOnly;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public ReadOnlyEntity getReadOnly() {
            return readOnly;
        }

        public void setReadOnly(ReadOnlyEntity readOnly) {
            this.readOnly = readOnly;
        }
    }

    /** The read-only property comes from the parent class. */
    public abstract static class ReadOnlyParent {
        @Column(name = "web_release")
        private boolean webRelease;

        public boolean isWebRelease() {
            return webRelease;
        }
    }

    @Table(name = "inherited_read_only_entity")
    public static class InheritedReadOnlyEntity extends ReadOnlyParent {
        @Id
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    /** The relation itself has a getter only. */
    @Table(name = "read_only_relation_entity")
    public static class ReadOnlyRelationEntity {
        @Id
        private Long id;
        @JoinColumn(name = "target_id")
        private TransientEntity target;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public TransientEntity getTarget() {
            return target;
        }
    }
}
