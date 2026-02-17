package org.ujorm.mapper.generated;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractKey;
import org.ujorm.core.impl.AbstractMetaModel;
import org.ujorm.mapper.generated.demo.City;
import org.ujorm.mapper.generated.demo.Employee;
import tools.Assert;

import java.time.LocalDate;
import java.util.List;

/** In generated class use a real package derived from the City class:
 * `org.ujorm._.org.ujorm.mapper.generated.demo`
 * */
public class City_ extends AbstractMetaModel<City> {

    private static final Class<City> domainType = City.class;

    public City_() {
        super( new Key_id(0)
             , new Key_name(1)
             , new Key_countryCode(2)
             , new Key_latitude(3)
             , new Key_longitude(4)
             );

    }


    @Override
    public City newDomain(@NotNull Object... values) {
        values = normalizePrimitives(values);
        return new City( (Long) values[0]
                       , (String) values[1]
                       , (String) values[2]
                       , (Double) values[3]
                       , (Double) values[4]
                       );
    }

    public static Object value(Key<Employee,Object> key, final Object value) {
        return value != null ? value : key.getDefaultValue();
    }

    @NotNull
    public Class<City> getDomainType() {
        return domainType;
    }

    /** ID key */
    static final class Key_id extends AbstractKey<City, Long> {

        public Key_id(int order) {
            super(order, "id", Long.class, "id", true, true);
        }
        @Override
        public void setValue(@NotNull final City bean, @Nullable final Long value) {
            throw unsupportedSetter(this);
        }
        @Override
        public Long getValue(@NotNull final City bean) {
            return bean.id();
        }
        @Override
        public @NotNull Class<City> getDomainType() {
            return domainType;
        }
    }

    /** name */
    static final class Key_name extends AbstractKey<City, String> {
        public Key_name(final int order) {
            super(order, "name", String.class, "name", false, true);
        }
        @Override
        public void setValue(@NotNull final City bean, @Nullable final String name) {
            throw unsupportedSetter(this);
        }
        @Override
        public String getValue(@NotNull final City bean) {
            return bean.name();
        }
        @Override
        public @NotNull Class<City> getDomainType() {
            return domainType;
        }
    }

    /** countryCode */
    static final class Key_countryCode extends AbstractKey<City, String> {
        public Key_countryCode(final int order) {
            super(order, "countryCode", String.class, "name", false, true);
        }

        @Override
        public void setValue(@NotNull final City bean, @Nullable final String value) {
            throw unsupportedSetter(this);
        }
        @Override
        public String getValue(@NotNull final City bean) {
            return bean.countryCode();
        }
        @Override
        public @NotNull Class<City> getDomainType() {
            return domainType;
        }
    }

    /** ID key */
    static final class Key_latitude extends AbstractKey<City, Double> {
        public Key_latitude(final int order) {
            super(order, "city", Double.class, "name", false, true);
        }
        @Override
        public void setValue(@NotNull final  City bean, @Nullable final Double value) {
            throw unsupportedSetter(this);
        }
        @Override
        public Double getValue(@NotNull final City bean) {
            return bean.latitude();
        }
        @Override
        public @NotNull Class<City> getDomainType() {
            return domainType;
        }
    }

    /** ID key */
    static final class Key_longitude extends AbstractKey<City, Double> {
        public Key_longitude(final int order) {
            super(order, "longitude", Double.class, "name", false, true);
        }
        @Override
        public void setValue(@NotNull final  City bean, @Nullable final Double value) {
            throw unsupportedSetter(this);
        }
        @Override
        public Double getValue(@NotNull final City bean) {
            return bean.latitude();
        }
        @Override
        public @NotNull Class<City> getDomainType() {
            return domainType;
        }
    }
}
