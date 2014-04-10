package com.github.dkharrat.nexusdata.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.database.sqlite.SQLiteDatabase;

public class SqlTableBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(SqlTableBuilder.class);

    private String tableName;
    private ArrayList<Column> columns;
    private ArrayList<TableConstraint> tableConstraints;

    public enum ColumnType {
        INTEGER,
        TEXT,
        BOOLEAN,
        DATETIME
    }

    public enum ConflictAction {
        REPLACE,
        ABORT,
        IGNORE
    }

    public enum Order {
        ASC,
        DESC,
        DEFAULT
    }

    public SqlTableBuilder() {

    }

    public SqlTableBuilder tableName(String name) {
        tableName = name;
        columns = new ArrayList<Column>();
        tableConstraints = new ArrayList<TableConstraint>();
        return this;
    }

    public SqlTableBuilder primaryKey(String name, ColumnType type) {
        return this.primaryKey(name, type, Order.DEFAULT, true);
    }

    public SqlTableBuilder primaryKey(String name, ColumnType type, Order order, boolean autoIncrement) {
        PrimaryKey primaryKeyContraint = new PrimaryKey();
        primaryKeyContraint.order = order;
        primaryKeyContraint.autoIncrement = autoIncrement;

        Column column = new Column(name, type);
        column.constraints.add(primaryKeyContraint);

        columns.add(column);

        return this;
    }

    public SqlTableBuilder column(String name, ColumnType type) {
        Column column = new Column(name, type);
        columns.add(column);
        return this;
    }

    public SqlTableBuilder setNullable(boolean nullable) {
        Column column = getLastColumn();
        Nullable nullableContraint = new Nullable(nullable);
        column.constraints.add(nullableContraint);

        return this;
    }

    public SqlTableBuilder setDefaultValue(String defaultValue) {
        Column column = getLastColumn();
        DefaultValue defaultValueContraint = new DefaultValue(defaultValue);
        column.constraints.add(defaultValueContraint);

        return this;
    }

    public SqlTableBuilder setUnique(ConflictAction conflictAction) {
        Column column = getLastColumn();
        Unique uniqueContraint = new Unique(new ConflictClause(conflictAction));
        column.constraints.add(uniqueContraint);

        return this;
    }

    public SqlTableBuilder setUnique(ConflictAction conflictAction, String... columns) {
        TableUniqueConstraint uniqueContraint = new TableUniqueConstraint(new ConflictClause(conflictAction), columns);
        tableConstraints.add(uniqueContraint);

        return this;
    }

    public SqlTableBuilder primaryKey(ConflictAction conflictAction, String... columns) {
        TablePrimaryKeyConstraint primaryKeyContraint = new TablePrimaryKeyConstraint(new ConflictClause(conflictAction), columns);
        tableConstraints.add(primaryKeyContraint);

        return this;
    }

    public void createTable(SQLiteDatabase db) {
        if (tableName == null)
            throw new IllegalStateException("Table name not specified");

        String sqlStatement = "CREATE TABLE " + tableName;

        if (columns.isEmpty())
            throw new IllegalStateException("No columns specified");

        sqlStatement += " (";
        ArrayList<String> columnsSqlStatements = new ArrayList<String>();
        for (Column column : columns) {
            columnsSqlStatements.add(column.toSql());
        }
        for (TableConstraint tableConstraint : tableConstraints) {
            columnsSqlStatements.add(tableConstraint.toSql());
        }
        sqlStatement += StringUtil.join(columnsSqlStatements, ", ") + ")";

        LOG.trace("Executing SQL Statement: " + sqlStatement);
        db.execSQL(sqlStatement);
    }

    // Note Sqlite does not support altering existing columns
    public void alterTable(SQLiteDatabase db) {
        if (tableName == null)
            throw new IllegalStateException("Table name not specified");

        for (Column column : columns) {
            String sqlStatement = "ALTER TABLE " + tableName + " ADD COLUMN " + column.toSql();

            LOG.trace("Executing SQL Statement: " + sqlStatement);
            db.execSQL(sqlStatement);
        }
    }

    private Column getLastColumn() {
        if (columns.isEmpty())
            throw new IllegalStateException("No column previously specified");
        else
            return columns.get(columns.size()-1);
    }

    private class Column {
        public String name;
        public ColumnType type;
        public ArrayList<ColumnConstraint> constraints = new ArrayList<ColumnConstraint>();

        public Column(String name, ColumnType type) {
            this.name = name;
            this.type = type;
        }

        public String toSql() {
            String sqlStatement = "'" + name + "' " + type.name() + " ";

            ArrayList<String> constraintSqlStatements = new ArrayList<String>();
            for (ColumnConstraint contraint : constraints) {
                constraintSqlStatements.add(contraint.toSql());
            }

            sqlStatement += StringUtil.join(constraintSqlStatements, " ");

            return sqlStatement;
        }
    }

    private class ConflictClause {
        public ConflictAction conflictAction;

        public ConflictClause(ConflictAction conflictAction) {
            this.conflictAction = conflictAction;
        }

        public String toSql() {
            return "ON CONFLICT " + conflictAction.name();
        }
    }

    private interface Constraint {
        String toSql();
    }

    private interface ColumnConstraint extends Constraint {
    }

    private interface TableConstraint extends ColumnConstraint {
    }

    private abstract class ColumnBasedTableConstraint implements TableConstraint {
        private final String contraintType;
        private final List<String> columns;
        private ConflictClause conflictClause = null;

        protected ColumnBasedTableConstraint(String constraintType, ConflictClause conflictClause, String... columns) {
            this.contraintType = constraintType;
            this.conflictClause = conflictClause;
            this.columns = Arrays.asList(columns);
        }

        @Override
        public String toSql() {
            String sqlStatement = contraintType;
            if (columns.isEmpty()) {
                throw new IllegalStateException("Table constraint's columns must not be empty");
            }

            sqlStatement += "(" + StringUtil.join(columns, ",") + ")";

            if (conflictClause != null) sqlStatement += " " + conflictClause.toSql();
            return sqlStatement;
        }
    }

    private class PrimaryKey implements ColumnConstraint {
        public Order order = null;
        public ConflictClause conflictClause = null;
        public boolean autoIncrement = false;

        @Override
        public String toSql() {
            String sqlStatement = "PRIMARY KEY";
            if (order != null && order != Order.DEFAULT) sqlStatement += " " + order.name();
            if (conflictClause != null) sqlStatement += " " + conflictClause.toSql();
            if (autoIncrement) sqlStatement += " AUTOINCREMENT";
            return sqlStatement;
        }
    }

    private class TablePrimaryKeyConstraint extends ColumnBasedTableConstraint {
        protected TablePrimaryKeyConstraint(ConflictClause conflictClause, String... columns) {
            super("PRIMARY KEY", conflictClause, columns);
        }
    }

    private class Nullable implements ColumnConstraint {
        public boolean nullable;
        public ConflictClause conflictClause = null;

        public Nullable(boolean nullable) {
            this.nullable = nullable;
        }

        @Override
        public String toSql() {
            String sqlStatement = "";
            if (!nullable) sqlStatement += " NOT NULL";
            if (conflictClause != null) sqlStatement += " " + conflictClause.toSql();
            return sqlStatement;
        }
    }

    private class DefaultValue implements ColumnConstraint {
        public String defaultValue;

        public DefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public String toSql() {
            return "DEFAULT '" + defaultValue + "'";
        }
    }

    private class Unique implements ColumnConstraint {
        public ConflictClause conflictClause;

        public Unique(ConflictClause conflictClause) {
            this.conflictClause = conflictClause;
        }

        @Override
        public String toSql() {
            return "UNIQUE " + conflictClause.toSql();
        }
    }

    private class TableUniqueConstraint extends ColumnBasedTableConstraint {
        protected TableUniqueConstraint(ConflictClause conflictClause, String... columns) {
            super("UNIQUE", conflictClause, columns);
        }
    }
}
