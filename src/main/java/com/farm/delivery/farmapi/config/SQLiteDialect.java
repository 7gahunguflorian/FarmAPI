package com.farm.delivery.farmapi.config;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupport;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;
import org.hibernate.type.SqlTypes;

import java.sql.Types;

public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();
    }

    @Override
    protected String columnType(int sqlTypeCode) {
        switch (sqlTypeCode) {
            case SqlTypes.BIT:
            case SqlTypes.BOOLEAN:
                return "integer";
            case SqlTypes.TINYINT:
                return "tinyint";
            case SqlTypes.SMALLINT:
                return "smallint";
            case SqlTypes.INTEGER:
                return "integer";
            case SqlTypes.BIGINT:
                return "bigint";
            case SqlTypes.FLOAT:
                return "float";
            case SqlTypes.REAL:
                return "real";
            case SqlTypes.DOUBLE:
                return "double";
            case SqlTypes.NUMERIC:
            case SqlTypes.DECIMAL:
                return "numeric";
            case SqlTypes.CHAR:
                return "char";
            case SqlTypes.VARCHAR:
                return "varchar";
            case SqlTypes.LONGVARCHAR:
                return "longvarchar";
            case SqlTypes.DATE:
                return "date";
            case SqlTypes.TIME:
                return "time";
            case SqlTypes.TIMESTAMP:
                return "timestamp";
            case SqlTypes.BINARY:
            case SqlTypes.VARBINARY:
            case SqlTypes.LONGVARBINARY:
            case SqlTypes.BLOB:
                return "blob";
            case SqlTypes.CLOB:
                return "clob";
            default:
                return super.columnType(sqlTypeCode);
        }
    }

    @Override
    public IdentityColumnSupport getIdentityColumnSupport() {
        return new SQLiteIdentityColumnSupport();
    }

    @Override
    public boolean hasAlterTable() {
        return false;
    }

    @Override
    public boolean dropConstraints() {
        return false;
    }

    @Override
    public String getAddColumnString() {
        return "add column";
    }

    @Override
    public boolean supportsOuterJoinForUpdate() {
        return false;
    }

    @Override
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    @Override
    public boolean supportsCascadeDelete() {
        return false;
    }

    @Override
    public String getDropForeignKeyString() {
        return "";
    }

    @Override
    public LimitHandler getLimitHandler() {
        return new LimitOffsetLimitHandler();
    }

    public static class SQLiteIdentityColumnSupport extends IdentityColumnSupportImpl {
        @Override
        public boolean supportsIdentityColumns() {
            return true;
        }

        @Override
        public String getIdentitySelectString(String table, String column, int type) {
            return "select last_insert_rowid()";
        }

        @Override
        public String getIdentityColumnString(int type) {
            return "integer";
        }
    }
}
