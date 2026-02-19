package org.ujorm.mapper.impl.dao;


import org.ujorm.mapper.demo.City;

import java.sql.Connection;
import java.sql.SQLException;


public class CityDao extends GenericCrudManager<City, Long> {

    final Connection dbConnection;

    public CityDao(Connection dbConnection) throws SQLException {
        super(dbConnection, City.class, City::setId);
        this.dbConnection = dbConnection;
    }

}
