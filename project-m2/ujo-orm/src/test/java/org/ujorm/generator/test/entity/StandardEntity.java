package org.ujorm.generator.test.entity;

import jakarta.persistence.Table;

/** Standard Java Bean with explicit getters and setters */
@Table(name = "standard_outer")
public class StandardEntity {

    private int id;
    private boolean active;
    private String name;
    private transient String ignoredTransient;
    private String ignoredNoSetter;
    private String ignoredNoGetter;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIgnoredTransient() { return ignoredTransient; }
    public void setIgnoredTransient(String ignoredTransient) { this.ignoredTransient = ignoredTransient; }

    // Missing setter -> should be ignored
    public String getIgnoredNoSetter() { return ignoredNoSetter; }

    // Missing getter -> should be ignored
    public void setIgnoredNoGetter(String ignoredNoGetter) { this.ignoredNoGetter = ignoredNoGetter; }

    /** Inner static class */
    @Table(name = "standard_inner")
    public static class InnerEntity {
        private int id;
        private boolean active;
        private String name;
        private transient String ignoredTransient;
        private String ignoredNoSetter;
        private String ignoredNoGetter;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getIgnoredTransient() { return ignoredTransient; }
        public void setIgnoredTransient(String ignoredTransient) { this.ignoredTransient = ignoredTransient; }

        public String getIgnoredNoSetter() { return ignoredNoSetter; }
        public void setIgnoredNoGetter(String ignoredNoGetter) { this.ignoredNoGetter = ignoredNoGetter; }
    }
}