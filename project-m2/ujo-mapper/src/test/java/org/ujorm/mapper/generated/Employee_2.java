package org.ujorm.mapper.generated;
//--- Generated ---

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractKey;
import org.ujorm.core.impl.AbstractMetaModel;

public class Employee_ extends AbstractMetaModel<org.ujorm.mapper.demo.Employee> {
    private static final Class<org.ujorm.mapper.demo.Employee> domainType = org.ujorm.mapper.demo.Employee.class;
    public Employee_() {
        super( new Key_id(0)
                , new Key_name(1)
                , new Key_superior(2)
                , new Key_city(3)
                , new Key_contractDay(4)
                , new Key_active(5)
        );
    }
    @Override
    public org.ujorm.mapper.demo.Employee newDomain(@NotNull final Object... values) {
        final var result = new org.ujorm.mapper.demo.Employee();
        for (int i = 0, max = Math.min(values.length, keyList.size()); i < max; i++) {
            final var key = (Key<org.ujorm.mapper.demo.Employee, Object>) keyList.get(i);
            key.setValue(result, values[i]);
        }
        return result;
    }
    @NotNull
    public Class<org.ujorm.mapper.demo.Employee> getDomainType() {
        return domainType;
    }
    /** Key id */
    static final class Key_id extends AbstractKey<org.ujorm.mapper.demo.Employee, class java.lang.Long> {
    public Key_id(int order) {
            super(order, "id", class java.lang.Long, "id", true, true);
        }
        @Override
        public void setValue(@NotNull final org.ujorm.mapper.demo.Employee bean, @Nullable final class java.lang.Long value) {
            bean.setid(value != null ? value : defaultValue);
        }
        @Override
        public class java.lang.Long getValue(@NotNull final org.ujorm.mapper.demo.Employee bean) {
            return bean.getid();
        }
        @Override
        public @NotNull Class<org.ujorm.mapper.demo.Employee> getDomainType() {
            return domainType;
        }
    }
    /** Key name */
    static final class Key_name extends AbstractKey<org.ujorm.mapper.demo.Employee, class java.lang.String> {
    public Key_name(int order) {
            super(order, "name", class java.lang.String, "name", true, true);
        }
        @Override
        public voname setValue(@NotNull final org.ujorm.mapper.demo.Employee bean, @Nullable final class java.lang.String value) {
            bean.setname(value != null ? value : defaultValue);
        }
        @Override
        public class java.lang.String getValue(@NotNull final org.ujorm.mapper.demo.Employee bean) {
            return bean.getname();
        }
        @Override
        public @NotNull Class<org.ujorm.mapper.demo.Employee> getDomainType() {
            return domainType;
        }
    }
    /** Key superior */
    static final class Key_superior extends AbstractKey<org.ujorm.mapper.demo.Employee, class org.ujorm.mapper.demo.Employee> {
    public Key_superior(int order) {
            super(order, "superior", class org.ujorm.mapper.demo.Employee, "superior", true, true);
        }
        @Override
        public vosuperior setValue(@NotNull final org.ujorm.mapper.demo.Employee bean, @Nullable final class org.ujorm.mapper.demo.Employee value) {
            bean.setsuperior(value != null ? value : defaultValue);
        }
        @Override
        public class org.ujorm.mapper.demo.Employee getValue(@NotNull final org.ujorm.mapper.demo.Employee bean) {
            return bean.getsuperior();
        }
        @Override
        public @NotNull Class<org.ujorm.mapper.demo.Employee> getDomainType() {
            return domainType;
        }
    }
    /** Key city */
    static final class Key_city extends AbstractKey<org.ujorm.mapper.demo.Employee, class org.ujorm.mapper.demo.City> {
    public Key_city(int order) {
            super(order, "city", class org.ujorm.mapper.demo.City, "city", true, true);
        }
        @Override
        public vocity setValue(@NotNull final org.ujorm.mapper.demo.Employee bean, @Nullable final class org.ujorm.mapper.demo.City value) {
            bean.setcity(value != null ? value : defaultValue);
        }
        @Override
        public class org.ujorm.mapper.demo.City getValue(@NotNull final org.ujorm.mapper.demo.Employee bean) {
            return bean.getcity();
        }
        @Override
        public @NotNull Class<org.ujorm.mapper.demo.Employee> getDomainType() {
            return domainType;
        }
    }
    /** Key contractDay */
    static final class Key_contractDay extends AbstractKey<org.ujorm.mapper.demo.Employee, class java.time.LocalDate> {
    public Key_contractDay(int order) {
            super(order, "contractDay", class java.time.LocalDate, "contractDay", true, true);
        }
        @Override
        public vocontractDay setValue(@NotNull final org.ujorm.mapper.demo.Employee bean, @Nullable final class java.time.LocalDate value) {
            bean.setcontractDay(value != null ? value : defaultValue);
        }
        @Override
        public class java.time.LocalDate getValue(@NotNull final org.ujorm.mapper.demo.Employee bean) {
            return bean.getcontractDay();
        }
        @Override
        public @NotNull Class<org.ujorm.mapper.demo.Employee> getDomainType() {
            return domainType;
        }
    }
    /** Key active */
    static final class Key_active extends AbstractKey<org.ujorm.mapper.demo.Employee, boolean> {
        public Key_active(int order) {
            super(order, "active", boolean, "active", true, true);
        }
        @Override
        public voactive setValue(@NotNull final org.ujorm.mapper.demo.Employee bean, @Nullable final boolean value) {
            bean.setactive(value != null ? value : defaultValue);
        }
        @Override
        public boolean getValue(@NotNull final org.ujorm.mapper.demo.Employee bean) {
            return bean.getactive();
        }
        @Override
        public @NotNull Class<org.ujorm.mapper.demo.Employee> getDomainType() {
            return domainType;
        }
    }
}