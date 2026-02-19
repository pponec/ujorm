package org.ujorm.mapper.impl;

import org.ujorm.mapper.JdbcMapper;

import java.sql.ResultSet;

public class JdbcMapperImpl<D> implements JdbcMapper<D> {

    /** Map ResultSet to domain object */
    public D map(ResultSet rs) {
        throw new UnsupportedOperationException("TODO");
    }
}
