package com.github.dkharrat.nexusdata.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import com.github.dkharrat.nexusdata.core.ManagedObject;
import com.github.dkharrat.nexusdata.metamodel.Entity;
import com.github.dkharrat.nexusdata.metamodel.ObjectModel;
import com.github.dkharrat.nexusdata.metamodel.Property;
import com.github.dkharrat.nexusdata.metamodel.Relationship;
import com.github.dkharrat.nexusdata.utils.SqlTableBuilder;
import com.github.dkharrat.nexusdata.utils.android.CursorUtil;
import com.github.dkharrat.nexusdata.utils.android.SQLiteDatabaseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

class DatabaseHelper extends SQLiteDatabaseHelper {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);

    private static final boolean DEBUG_QUERIES = false;

    private static final String METADATA_TABLE_NAME = "nxs_metadata";
    private static final String METADATA_COLUMN_VERSION = "version";
    private static final String METADATA_COLUMN_UUID = "uuid";

    private static final String ENTITY_TABLE_NAME = "nxs_entity";
    private static final String ENTITY_COLUMN_ID = "_id";
    private static final String ENTITY_COLUMN_NAME = "name";

    private ObjectModel model;

    DatabaseHelper(Context context, File path, ObjectModel model) {
        super(context, path, DEBUG_QUERIES ? new SQLiteCursorLoggerFactory() : null, model.getVersion());
        this.model = model;
    }

    static <T extends ManagedObject> String getTableName(Entity<T> entity) {
        return entity.getTopMostSuperEntity().getType().getSimpleName();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LOG.info("Creating database: " + db.getPath());

        createMetadataTable(db);
        createEntityInfoTable(db);

        Map<Entity<?>, Integer> entityIDMap = generateEntityIDs(db);

        for (Entity<?> entity : model.getEntities()) {
            if (!entity.isBaseEntity()) {
                continue;
            }
            createEntityTable(db, entity, entityIDMap);
        }

        generateMetadata(db);
    }

    private void createMetadataTable(SQLiteDatabase db) {
        // create the metadata table
        SqlTableBuilder tableBuilder = new SqlTableBuilder();
        tableBuilder.tableName(METADATA_TABLE_NAME);
        tableBuilder.column(METADATA_COLUMN_VERSION, SqlTableBuilder.ColumnType.INTEGER).setUnique(SqlTableBuilder.ConflictAction.ABORT);
        tableBuilder.column(METADATA_COLUMN_UUID, SqlTableBuilder.ColumnType.TEXT);
        tableBuilder.createTable(db);
    }

    private void createEntityInfoTable(SQLiteDatabase db) {
        SqlTableBuilder tableBuilder = new SqlTableBuilder();
        tableBuilder.tableName(ENTITY_TABLE_NAME);
        tableBuilder.primaryKey(ENTITY_COLUMN_ID, SqlTableBuilder.ColumnType.INTEGER);
        tableBuilder.column(ENTITY_COLUMN_NAME, SqlTableBuilder.ColumnType.TEXT);
        tableBuilder.createTable(db);
    }

    private void createEntityTable(SQLiteDatabase db, Entity<?> entity, Map<Entity<?>, Integer> entityIDMap) {
        SqlTableBuilder tableBuilder = new SqlTableBuilder();
        tableBuilder.tableName(getTableName(entity));
        tableBuilder.primaryKey(AndroidSqlPersistentStore.ID_COLUMN_NAME, SqlTableBuilder.ColumnType.INTEGER);
        tableBuilder.column(AndroidSqlPersistentStore.ENTITY_COLUMN_NAME, SqlTableBuilder.ColumnType.INTEGER).setNullable(false);

        for (Property property : Utils.getPropertiesOfEntityAndItsChildren(entity)) {
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
            } else if (float.class.isAssignableFrom(propType) || Float.class.isAssignableFrom(propType)) {
                columnType = SqlTableBuilder.ColumnType.REAL;
            } else if (double.class.isAssignableFrom(propType) || Double.class.isAssignableFrom(propType)) {
                columnType = SqlTableBuilder.ColumnType.REAL;
            } else if (Date.class.isAssignableFrom(property.getType())) {
                columnType = SqlTableBuilder.ColumnType.DATETIME;
            } else {
                throw new UnsupportedOperationException("Unsupported field type " + property.getType() + " for " + entity.getType());
            }

            int entityID = entityIDMap.get(property.getEntity());
            tableBuilder.column(property.getName() + "_" + entityID, columnType);
        }

        tableBuilder.createTable(db);
    }

    private Map<Entity<?>, Integer> generateEntityIDs(SQLiteDatabase db) {
        Map<Entity<?>, Integer> entityIDMap = new HashMap<>();
        int entityID = 1;
        for (Entity<?> entity : model.getEntities()) {
            ContentValues entityMetadataValues = new ContentValues();
            entityMetadataValues.put(ENTITY_COLUMN_ID, entityID);
            entityMetadataValues.put(ENTITY_COLUMN_NAME, entity.getName());
            db.insert(ENTITY_TABLE_NAME, null, entityMetadataValues);
            entityIDMap.put(entity, entityID);
            entityID++;
        }

        return entityIDMap;
    }

    Map<Entity<?>, Integer> getEntityIDs(SQLiteDatabase db) {
        Map<Entity<?>, Integer> entityIDs = new HashMap<>();

        Cursor cursor = db.query(ENTITY_TABLE_NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int entityID = CursorUtil.getInt(cursor, ENTITY_COLUMN_ID);
            String entityName = CursorUtil.getString(cursor, ENTITY_COLUMN_NAME);
            entityIDs.put(model.getEntity(entityName), entityID);
        }

        return entityIDs;
    }

    private UUID generateMetadata(SQLiteDatabase db) {
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

    private void clearDatabase(SQLiteDatabase db) {
        dropTables(db);
        onCreate(db);
    }

    private static void dropTables(SQLiteDatabase db) {
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

    private static class SQLiteCursorLoggerFactory implements SQLiteDatabase.CursorFactory {

        @SuppressWarnings("deprecation")
        @Override
        public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
                                String editTable, SQLiteQuery query) {
            LOG.debug(query.toString());

            // non-deprecated API is only available in API 11
            return new SQLiteCursor(db, masterQuery, editTable, query);
        }
    }
}
