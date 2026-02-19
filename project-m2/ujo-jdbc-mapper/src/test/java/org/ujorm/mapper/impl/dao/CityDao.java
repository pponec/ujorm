package org.ujorm.mapper.impl.dao;


import org.ujorm.core.DomainHandler;
import org.ujorm.mapper.core.CrudService;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.impl.MapperContext;
import org.ujorm.tools.jdbc.SqlParamBuilder;

import java.sql.Connection;
import java.sql.SQLException;


public abstract class CityDao extends CrudService<City, Long> {

    final Connection dbConnection;

    public CityDao(SqlParamBuilder sqlBuilder, DomainHandler<City> domainHandler, MapperContext mapperContext, Connection dbConnection) {
        super(sqlBuilder, domainHandler, mapperContext);
        this.dbConnection = dbConnection;
    }

//    public CityDao(Connection dbConnection) throws SQLException {
//        super(dbConnection, City.class, null /*City::setId*/ ); // TODO:pop
//        this.dbConnection = dbConnection;
//    }

}
