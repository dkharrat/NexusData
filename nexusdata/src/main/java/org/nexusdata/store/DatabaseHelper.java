package org.nexusdata.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.nexusdata.core.ManagedObject;
import org.nexusdata.metamodel.Entity;
import org.nexusdata.metamodel.ObjectModel;
import org.nexusdata.metamodel.Property;
import org.nexusdata.metamodel.Relationship;
import org.nexusdata.utils.SqlTableBuilder;
import org.nexusdata.utils.android.CursorUtil;
import org.nexusdata.utils.android.SQLiteDatabaseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.UUID;

class DatabaseHelper extends SQLiteDatabaseHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final boolean DEBUG_QUERIES = false;

    private static final String METADATA_TABLE_NAME = "nxs_metadata";
    private static final String METADATA_COLUMN_VERSION = "version";
    private static final String METADATA_COLUMN_UUID = "uuid";

    ObjectModel model;

    DatabaseHelper(Context context, File path, ObjectModel model) {
        super(context, path, DEBUG_QUERIES ? new AndroidSqlPersistentStore.SQLiteCursorLoggerFactory() : null, model.getVersion());
        this.model = model;
    }

    static <T extends ManagedObject> String getTableName(Entity<T> entity) {
        return entity.getType().getSimpleName();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LOG.info("Creating database: " + db.getPath());

        // create the metadata table
        SqlTableBuilder tableBuilder = new SqlTableBuilder();
        tableBuilder.tableName(METADATA_TABLE_NAME);
        tableBuilder.column(METADATA_COLUMN_VERSION, SqlTableBuilder.ColumnType.INTEGER).setUnique(SqlTableBuilder.ConflictAction.ABORT);
        tableBuilder.column(METADATA_COLUMN_UUID, SqlTableBuilder.ColumnType.TEXT);
        tableBuilder.createTable(db);

        for (Entity<?> entity : model.getEntities()) {
            tableBuilder = new SqlTableBuilder();
            tableBuilder.tableName(getTableName(entity));
            tableBuilder.primaryKey(AndroidSqlPersistentStore.COLUMN_ID_NAME, SqlTableBuilder.ColumnType.INTEGER);

            for (Property property : entity.getProperties()) {
                SqlTableBuilder.ColumnType columnType;
                Class<?> propType = property.getType();

                if (property.isRelationship()) {
                    Relationship relationship = (Relationship) property;
                    if (relationship.isToOne()) {
                        columnType = SqlTableBuilder.ColumnType.INTEGER;
                    } else {
                        continue;
                    }
                }
                else if (int.class.isAssignableFrom(propType) || Integer.class.isAssignableFrom(propType) ||
                        long.class.isAssignableFrom(propType) || Long.class.isAssignableFrom(propType)) {
                    columnType = SqlTableBuilder.ColumnType.INTEGER;
                } else if (String.class.isAssignableFrom(propType) || Enum.class.isAssignableFrom(propType)) {
                    columnType = SqlTableBuilder.ColumnType.TEXT;
                } else if (boolean.class.isAssignableFrom(propType) || Boolean.class.isAssignableFrom(propType)) {
                    columnType = SqlTableBuilder.ColumnType.BOOLEAN;
                } else if (Date.class.isAssignableFrom(property.getType())) {
                    columnType = SqlTableBuilder.ColumnType.DATETIME;
                } else {
                    throw new UnsupportedOperationException("Unsupported field type " + property.getType() + " for " + entity.getType());
                }

                tableBuilder.column(property.getName(), columnType);
                if (property.isRequired()) {
                    tableBuilder.setNullable(false);
                }
            }

            tableBuilder.createTable(db);
        }

        generateMetadata(db);
    }

    UUID generateMetadata(SQLiteDatabase db) {
        UUID uuid = UUID.randomUUID();

        ContentValues metadataValues = new ContentValues();
        metadataValues.put(METADATA_COLUMN_VERSION, model.getVersion());
        metadataValues.put(METADATA_COLUMN_UUID, uuid.toString());
        db.insert(METADATA_TABLE_NAME, null, metadataValues);

        return uuid;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOG.info("Upgrading DB from " + oldVersion + " to " + newVersion);

        //TODO: ideally, DB should be migrated to newer version as opposed to re-creating it
        clearDatabase(db);
    }

    void clearDatabase(SQLiteDatabase db) {
        dropTables(db);
        onCreate(db);
    }

    static void dropTables(SQLiteDatabase db) {
        String TABLES_SQL = "select 'drop table if exists ' || name || ';' from sqlite_master where type='table' "+
                "and name not like 'android%' "+
                "and name not like 'sqlite%';"+
                "and name not like '"+METADATA_TABLE_NAME+"';";
        Cursor c = db.rawQuery(TABLES_SQL, null);
        while(c.moveToNext()) {
            String dropTableSql = c.getString(0);
            LOG.info("Executing: " + dropTableSql);
            db.execSQL(dropTableSql);
        }
    }

    static UUID getDatabaseUuid(SQLiteDatabase db, int version) {
        UUID uuid = null;
        Cursor cursor = db.query(METADATA_TABLE_NAME, null, METADATA_COLUMN_VERSION+"=?", new String[]{String.valueOf(version)}, null, null, null);
        if (cursor.moveToFirst()) {
            uuid = UUID.fromString(CursorUtil.getString(cursor, METADATA_COLUMN_UUID));
        }
        return uuid;
    }
}
