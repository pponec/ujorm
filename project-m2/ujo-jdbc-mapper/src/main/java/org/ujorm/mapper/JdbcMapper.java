package org.ujorm.mapper;

import java.sql.ResultSet;

public interface JdbcMapper<D> {

    /** Map ResultSet to domain object */
    D map(ResultSet rs);
}
