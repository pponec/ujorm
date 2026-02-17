package org.ujorm.mapper.generated;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractKey;
import org.ujorm.core.impl.AbstractMetaModel;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;

import java.time.LocalDate;

/** In generated class use a real package derived from the Employee class:
 * `org.ujorm._org.ujorm.mapper.generated.demo`
 * */
public class Employee_ extends AbstractMetaModel<Employee> {

    private static final Class<Employee> domainType = Employee.class;

    public Employee_() {
        super( new Key_id(0)
             , new Key_name(1)
             , new Key_city(2)
             , new Key_superior(3)
             , new Key_contractDay(4)
             , new Key_active(5)
             );
    }

    @Override
    public Employee newDomain(@NotNull final Object... values) {
        final var result = new Employee();
        for (int i = 0, max = Math.min(values.length, keyList.size()); i < max; i++) {
            final var key = (Key<Employee, Object>) keyList.get(i);
            key.setValue(result, values[i]);
        }
        return result;
    }

    @NotNull
    public Class<Employee> getDomainType() {
        return domainType;
    }

    /** Key id */
    static final class Key_id extends AbstractKey<Employee, Long> {

        public Key_id(int order) {
            super(order, "id", Long.class, "id", true, true);
        }
        @Override
        public void setValue(@NotNull final Employee bean, @Nullable final Long value) {
            bean.setId(value != null ? value : defaultValue);
        }
        @Override
        public Long getValue(@NotNull final Employee bean) {
            return bean.getId();
        }
        @Override
        public @NotNull Class<Employee> getDomainType() {
            return domainType;
        }
    }

    /** Key name */
    static final class Key_name extends AbstractKey<Employee, String> {
        public Key_name(final int order) {
            super(order, "name", String.class, "name", false, true);
        }
        @Override
        public void setValue(@NotNull final Employee bean, @Nullable final String name) {
            bean.setName(name);
        }
        @Override
        public String getValue(@NotNull final Employee bean) {
            return bean.getName();
        }
        @Override
        public @NotNull Class<Employee> getDomainType() {
            return domainType;
        }
    }

    /** ID key */
    static final class Key_superior extends AbstractKey<Employee, Employee> {
        public Key_superior(final int order) {
            super(order, "superior", Employee.class, "name", false, false);
        }

        @Override
        public void setValue(@NotNull final Employee bean, @Nullable final Employee value) {
            bean.setSuperior(value);
        }
        @Override
        public Employee getValue(@NotNull final Employee bean) {
            return bean.getSuperior();
        }
        @Override
        public @NotNull Class<Employee> getDomainType() {
            return domainType;
        }
    }

    /** ID city */
    static final class Key_city extends AbstractKey<Employee, City> {
        public Key_city(final int order) {
            super(order, "city", City.class, "name", false, true);
        }
        @Override
        public void setValue(@NotNull final  Employee bean, @Nullable final City value) {
            bean.setCity(value);
        }
        @Override
        public City getValue(@NotNull final Employee bean) {
            return bean.getCity();
        }
        @Override
        public @NotNull Class<Employee> getDomainType() {
            return domainType;
        }
    }

    static final class Key_contractDay extends AbstractKey<Employee, LocalDate> {
        public Key_contractDay(final int order) {
            super(order, "contractDay", LocalDate.class, "contractDay", false, true);
        }

        @Override
        public void setValue(@NotNull Employee bean, @Nullable LocalDate value) {
            bean.setContractDay(value);
        }
        @Override
        public LocalDate getValue(@NotNull Employee bean) {
            return bean.getContractDay();
        }
        @Override
        public @NotNull Class<Employee> getDomainType() {
            return domainType;
        }
    }

    static final class Key_active extends AbstractKey<Employee, Boolean> {
        public Key_active(final int order) {
            super(order, "active", boolean.class, "contractDay", false, true);
        }

        @Override
        public void setValue(@NotNull final Employee bean, @Nullable final Boolean value) {
            bean.setActive(value != null ? value : defaultValue);
        }
        @Override
        public Boolean getValue(@NotNull final Employee bean) {
            return bean.isActive();
        }
        @Override
        public @NotNull Class<Employee> getDomainType() {
            return domainType;
        }
    }
}
