package org.ujorm.orm.model;

public record Jdbc(
        DatabaseVendor dbVendor,
        QuotePair quotes)
{
    public boolean isOracleDb() {
        return dbVendor == DatabaseVendor.ORACLE;
    }

    public boolean isMsSqlSrv() {
        return dbVendor == DatabaseVendor.MS_SQL_SERVER;
    }

}
