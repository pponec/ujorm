package org.ujorm.mapper.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.ujorm.mapper.MapperContext;

/** TODO:pop: create interface from this class */
@Setter @Getter @ToString
public class Config implements MapperContext {

    /** The first key in the sequence represents the primary key. */
    private boolean firstPropertyIsIdentifier = true;

    /** Maximum size of the cache in the ResultSet Mapper */
    private int maxCacheSize = 512;

    /** Batch size for the INSERT */
    private int insertBatchSize = 512;

    /** Printa all SQL template to the log. */
    private boolean printSql = true;

    /** Enable quoting the SQL columns */
    boolean enableSqlQuoting = false;

    /** Write a warning if the column is not a relation and has no JDBC mapping. */
    boolean columnMappingWarning = true;

}
