package org.nexusdata.store;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.nexusdata.metamodel.*;
import org.nexusdata.utils.StringUtil;
import org.nexusdata.utils.android.SQLiteDatabaseHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;

import org.nexusdata.core.FetchRequest;
import org.nexusdata.core.IncrementalStore;
import org.nexusdata.core.ManagedObject;
import org.nexusdata.core.ObjectContext;
import org.nexusdata.core.ObjectID;
import org.nexusdata.core.SaveChangesRequest;
import org.nexusdata.core.SortDescriptor;
import org.nexusdata.core.StoreCacheNode;
import org.nexusdata.predicate.ComparisonPredicate;
import org.nexusdata.predicate.FieldPathExpression;
import org.nexusdata.predicate.Predicate;
import org.nexusdata.utils.android.CursorUtil;
import org.nexusdata.utils.DateUtil;
import org.nexusdata.utils.android.SQLiteDatabaseHelper;
import org.nexusdata.utils.SqlTableBuilder;
import org.nexusdata.utils.SqlTableBuilder.ColumnType;
import org.nexusdata.utils.SqlTableBuilder.ConflictAction;
import org.nexusdata.utils.StringUtil;

/* TODO: AndroidSqlPersistentStore changes
 *  - improve memory-management
 *  - code clean-up (see todo's in code below)
 */

public class AndroidSqlPersistentStore extends IncrementalStore {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidSqlPersistentStore.class);

    static final String COLUMN_ID_NAME = "_ID";

    DatabaseHelper m_databaseHelper;
    Map<String,Long> m_lastRowIDs = new HashMap<String,Long>();
    Context m_context;

    SQLiteDatabase m_db;

    // TODO: use a MRU cache and also remove objects if they are unregistered from all contexts
    Map<Class<?>, Map<Long,StoreCacheNode>> m_cache = new HashMap<Class<?>, Map<Long,StoreCacheNode>>();

    public AndroidSqlPersistentStore(Context context, File path) {
        super(path);
        m_context = context;
    }

    @Override
    protected void loadMetadata() {
        ObjectModel model = getCoordinator().getModel();
        m_databaseHelper = new DatabaseHelper(m_context, getLocation(), model);

        // TODO: does the DB need to be closed at some point?
        m_db = m_databaseHelper.getWritableDatabase();

        setUuid(DatabaseHelper.getDatabaseUuid(m_db, model.getVersion()));
    }

    static protected String getDatabaseName(ObjectModel model) {
        return model.getClass().getSimpleName() + ".db";
    }

    static protected String getColumnName(PropertyDescription property) {
        return "`" + property.getName() + "`";
    }

    private <T extends ManagedObject> T createObjectFromCursor(ObjectContext context, EntityDescription<T> entity, Cursor cursor) {

        long id = CursorUtil.getLong(cursor, COLUMN_ID_NAME);
        ObjectID objectID = this.createObjectID(entity, id);
        T object = context.objectWithID(objectID);

        StoreCacheNode cacheNode = getStoreNodeFromCursor(objectID, cursor, context);
        Map<Long,StoreCacheNode> entityCache = m_cache.get(entity.getType());
        if (entityCache == null) {
            entityCache = new HashMap<Long,StoreCacheNode>();
            m_cache.put(entity.getType(), entityCache);
        }
        entityCache.put(id, cacheNode);

        return object;
    }

    private <T extends ManagedObject> Cursor performQuery(SQLiteDatabase db, FetchRequest<T> request) {

        // TODO: this is a pretty crude and limited predicate-to-SQL converter; need to build a proper one

        String limit = null;
        if (request.getLimit() != Integer.MAX_VALUE) {
            limit = String.valueOf(request.getLimit());
        }

        String orderBy = null;
        if (request.hasSortDescriptors()) {
            List<String> orderBys = new ArrayList<String>();
            for (SortDescriptor sortDesc : request.getSortDescriptors()) {
                String orderType = sortDesc.isAscending() ? " ASC" : " DESC";
                orderBys.add(sortDesc.getAttributeName() + orderType);
            }

            orderBy = StringUtil.join(orderBys, ",");
        }

        String selection = "";
        ArrayList<String> selectionArgs = new ArrayList<String>();
        if (request.getPredicate() != null) {
            Predicate predicate = request.getPredicate();

            if (predicate instanceof ComparisonPredicate) {
                ComparisonPredicate comparison = (ComparisonPredicate)predicate;

                if (comparison.getLhs() instanceof FieldPathExpression) {
                    selection += "`" + ((FieldPathExpression)comparison.getLhs()).getFieldPath() + "`";
                    switch (comparison.getOperator()) {
                        case EQUAL:
                            selection += "=?";
                            break;
                        default:
                            throw new UnsupportedOperationException("Not yet implemented");
                    }
                    Object rhs = comparison.getRhs().evaluate(null);
                    if (rhs instanceof ManagedObject) {
                        ManagedObject relatedObject = (ManagedObject)rhs;
                        selectionArgs.add(getReferenceObjectForObjectID(relatedObject.getID()).toString());
                    } else {
                        String value = rhs.toString();
                        if (rhs.getClass().isAssignableFrom(Boolean.class) || rhs.getClass().isAssignableFrom(boolean.class)) {
                            value = ((Boolean)rhs) ? "1" : "0";
                        }
                        selectionArgs.add(value);
                    }
                }
            }
        }

        if (selection.isEmpty()) {
            selection = null;
            selectionArgs = null;
        }

        Cursor cursor = db.query(
                false,          // not distinct
                getTableName(request.getEntity()),
                null, // columns
                selection,      // selection
                selectionArgs == null ? null : selectionArgs.toArray(new String[0]),           // selectionArgs
                null,           // groupBy
                null,           // having
                orderBy,        // orderBy
                limit);         // limit

        return cursor;
    }

    @Override
    protected <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest<T> request, ObjectContext context) {
        Cursor cursor = performQuery(m_db, request);

        List<T> results = new ArrayList<T>();
        while(cursor.moveToNext()) {
            T object = createObjectFromCursor(context, request.getEntity(), cursor);
            results.add(object);
        }

        cursor.close();

        return results;
    }

    private ContentValues getContentValues(ManagedObject object) throws IllegalArgumentException, IllegalAccessException {
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID_NAME, getReferenceObjectForObjectID(object.getID()).toString());
        for (PropertyDescription property : object.getEntity().getProperties()) {
            Class<?> propertyType = property.getType();
            Object value = object.getValue(property.getName());

            if (property.isRelationship()) {
                RelationshipDescription relationship = (RelationshipDescription)property;
                if (relationship.isToOne()) {
                    ManagedObject toOneObject = (ManagedObject) value;
                    if (toOneObject != null) {
                        values.put(getColumnName(relationship), getReferenceObjectForObjectID(toOneObject.getID()).toString());
                    } else {
                        values.putNull(getColumnName(relationship));
                    }
                }
            } else {
                if (value != null) {
                    if (Date.class.isAssignableFrom(propertyType)) {
                        values.put(getColumnName(property), DateUtil.format(DateUtil.ISO8601_NO_TIMEZONE, (Date)value));
                    } else if (Boolean.class.isAssignableFrom(propertyType)) {
                        values.put(getColumnName(property), ((Boolean)value) ? "1" : "0" );
                    } else {
                        values.put(getColumnName(property), value.toString());
                    }
                } else {
                    values.putNull(getColumnName(property));
                }
            }
        }

        return values;
    }

    @Override
    protected void executeSaveRequest(SaveChangesRequest request, ObjectContext context) {
        m_db.beginTransaction();
        try {
            for (ManagedObject object : request.getInsertedObjects()) {
                ContentValues values = getContentValues(object);
                //TODO: log inserts, updates & deletes
                m_db.insertOrThrow(getTableName(object.getEntity()), null, values);
            }

            for (ManagedObject object : request.getUpdatedObjects()) {
                ContentValues values = getContentValues(object);
                long id = (Long)getReferenceObjectForObjectID(object.getID());
                m_db.update(getTableName(object.getEntity()), values, "_ID = " + id, null);

                Map<Long, StoreCacheNode> entityCache = m_cache.get(object.getEntity().getType());
                if (entityCache != null) {
                    //TODO: update cache entry instead of deleting it
                    entityCache.remove(id);
                }
            }

            for (ManagedObject object : request.getDeletedObjects()) {
                long id = (Long)getReferenceObjectForObjectID(object.getID());
                m_db.delete(getTableName(object.getEntity()), "_ID = " + id, null);

                Map<Long, StoreCacheNode> entityCache = m_cache.get(object.getEntity().getType());
                if (entityCache != null) {
                    entityCache.remove(id);
                }
            }

            m_db.setTransactionSuccessful();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            m_db.endTransaction();
        }
    }

    private StoreCacheNode getStoreNodeFromCursor(ObjectID objectID, Cursor cursor, ObjectContext context) {
        StoreCacheNode node = new StoreCacheNode(objectID);

        try {
            for (PropertyDescription property : objectID.getEntity().getProperties()) {
                Object value;
                Class<?> propType = property.getType();

                if (property.isRelationship()) {
                    RelationshipDescription relationship = (RelationshipDescription)property;
                    if (relationship.isToOne()) {
                        EntityDescription<?> assocEntity = getCoordinator().getModel().getEntity((Class<ManagedObject>)relationship.getType());
                        long relatedID = CursorUtil.getLong(cursor, relationship.getName());
                        if (relatedID != 0) {
                            value = this.createObjectID(assocEntity, relatedID);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else if (CursorUtil.isNull(cursor, property.getName())) {
                    value = null;
                } else if (propType.isAssignableFrom(Integer.class) || propType.isAssignableFrom(int.class)) {
                    value = CursorUtil.getInt(cursor, property.getName());
                } else if (propType.isAssignableFrom(Long.class) || propType.isAssignableFrom(long.class)) {
                    value = CursorUtil.getLong(cursor, property.getName());
                } else if (propType.isAssignableFrom(String.class)) {
                    value = CursorUtil.getString(cursor, property.getName());
                } else if (propType.isAssignableFrom(Boolean.class) || propType.isAssignableFrom(boolean.class)) {
                    value = CursorUtil.getBoolean(cursor, property.getName());
                } else if (Enum.class.isAssignableFrom(propType)) {
                    String enumName = CursorUtil.getString(cursor, property.getName());
                    if (enumName != null) {
                        value = Enum.valueOf((Class<? extends Enum>)propType, enumName);
                    } else {
                        value = null;
                    }
                } else if (propType.isAssignableFrom(Date.class)) {
                    String dateStr = CursorUtil.getString(cursor, property.getName());
                    if (dateStr != null) {
                        value = DateUtil.parse(DateUtil.ISO8601_NO_TIMEZONE, dateStr);
                    } else {
                        value = null;
                    }
                } else {
                    throw new UnsupportedOperationException("Unsupported property type " + property.getType());
                }

                node.setProperty(property.getName(), value);
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return node;
    }

    @Override
    protected StoreCacheNode getObjectValues(ObjectID objectID, ObjectContext context) {

        long id = Long.valueOf(getReferenceObjectForObjectID(objectID).toString());

        Map<Long,StoreCacheNode> entityCache = m_cache.get(objectID.getEntity().getType());
        if (entityCache != null) {
            StoreCacheNode node = entityCache.get(id);
            if (node != null) {
                return node;
            }
        }

        Cursor cursor = m_db.query(
                false,          // not distinct
                getTableName(objectID.getEntity()),
                null,           // columns
                COLUMN_ID_NAME + "=?",           // selection
                new String[]{String.valueOf(id)},           // selectionArgs
                null,           // groupBy
                null,           // having
                null,           // orderBy
                null);          // limit

        StoreCacheNode node = null;
        if (cursor.moveToNext()) {
            node = getStoreNodeFromCursor(objectID, cursor, context);
        }
        cursor.close();

        if (entityCache == null) {
            entityCache = new HashMap<Long,StoreCacheNode>();
            m_cache.put(objectID.getEntity().getType(), entityCache);
        }
        entityCache.put(id, node);

        return node;
    }

    @Override
    protected Collection<ObjectID> getToManyRelationshipValue(
            ObjectID objectID,
            RelationshipDescription relationship,
            ObjectContext context) {

        String[] columns = new String[]{COLUMN_ID_NAME};
        String table = getTableName(relationship.getDestinationEntity());
        String selection = relationship.getInverse().getName()+"=?";
        String[] selectionArgs = new String[]{getReferenceObjectForObjectID(objectID).toString()};

        Cursor cursor = m_db.query(
                false,          // not distinct
                table,
                columns,        // columns
                selection,      // selection
                selectionArgs,  // selectionArgs
                null,           // groupBy
                null,           // having
                null,           // orderBy
                null);          // limit


        List<ObjectID> results = new ArrayList<ObjectID>();
        while(cursor.moveToNext()) {
            long id = CursorUtil.getLong(cursor, COLUMN_ID_NAME);
            ObjectID relatedObject = this.createObjectID(relationship.getDestinationEntity(), id);
            results.add(relatedObject);
        }
        cursor.close();

        return results;
    }

    @Override
    protected ObjectID getToOneRelationshipValue(
            ObjectID objectID,
            RelationshipDescription relationship,
            ObjectContext context) {

        String fromTable = getTableName(objectID.getEntity());
        String toTable = getTableName(relationship.getDestinationEntity());

        String table = fromTable + " t1," + toTable + " t2";
        String[] columns = new String[]{"t1" + "." + COLUMN_ID_NAME};
        String selection = "t1"+"."+COLUMN_ID_NAME+"="+getReferenceObjectForObjectID(objectID) + " AND " +
                           "t1"+"."+getColumnName(relationship)+"=t2."+COLUMN_ID_NAME;

        Cursor cursor = m_db.query(
                false,          // not distinct
                table,
                columns,           // columns
                selection,     // selection
                null,           // selectionArgs
                null,           // groupBy
                null,           // having
                null,           // orderBy
                null);          // limit


        ObjectID relatedObjectID = null;
        if(cursor.moveToNext()) {
            long id = CursorUtil.getLong(cursor, COLUMN_ID_NAME);
            relatedObjectID = this.createObjectID(relationship.getDestinationEntity(), id);
        }
        cursor.close();

        return relatedObjectID;
    }

    private long getLastRowIDFromDatabase(SQLiteDatabase db, String tableName) {
        long lastRow = 1;
        Cursor cursor = db.query(
                false,          // not distinct
                "sqlite_sequence",
                null,           // columns
                "name='" + tableName + "'",           // selection
                null,           // selectionArgs
                null,           // groupBy
                null,           // having
                null,           // orderBy
                "1");           // limit

        if (cursor.moveToNext()) {
            lastRow = CursorUtil.getLong(cursor, "seq")+1;
        }
        cursor.close();

        return lastRow;
    }

    @Override
    protected List<ObjectID> getPermanentIDsForObjects(List<ManagedObject> objects) {

        List<ObjectID> objectIDs = new ArrayList<ObjectID>();
        for (ManagedObject object : objects) {
            ObjectID id;

            String tableName = getTableName(object.getEntity());
            Long lastRowID = m_lastRowIDs.get(tableName);
            if (lastRowID == null) {
                lastRowID = getLastRowIDFromDatabase(m_db, tableName);
            }
            id = createObjectID(object.getEntity(), lastRowID++);
            m_lastRowIDs.put(getTableName(object.getEntity()), lastRowID);

            objectIDs.add(id);
        }

        return objectIDs;
    }

    static private <T extends ManagedObject> String getTableName(EntityDescription<T> entity) {
        return entity.getType().getSimpleName();
    }

    static class DatabaseHelper extends SQLiteDatabaseHelper {

        private static final Logger LOG = LoggerFactory.getLogger(DatabaseHelper.class);

        private static final boolean DEBUG_QUERIES = false;

        private static final String METADATA_TABLE_NAME = "nxs_metadata";
        private static final String METADATA_COLUMN_VERSION = "version";
        private static final String METADATA_COLUMN_UUID = "uuid";

        ObjectModel m_model;

        DatabaseHelper(Context context, File path, ObjectModel model) {
            super(context, path, DEBUG_QUERIES ? new SQLiteCursorLoggerFactory() : null, model.getVersion());
            m_model = model;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            LOG.info("Creating database: " + db.getPath());

            // create the metadata table
            SqlTableBuilder tableBuilder = new SqlTableBuilder();
            tableBuilder.tableName(METADATA_TABLE_NAME);
            tableBuilder.column(METADATA_COLUMN_VERSION, ColumnType.INTEGER).setUnique(ConflictAction.ABORT);
            tableBuilder.column(METADATA_COLUMN_UUID, ColumnType.TEXT);
            tableBuilder.createTable(db);

            for (EntityDescription<?> entity : m_model.getEntities()) {
                tableBuilder = new SqlTableBuilder();
                tableBuilder.tableName(getTableName(entity));
                tableBuilder.primaryKey(COLUMN_ID_NAME, ColumnType.INTEGER);

                for (PropertyDescription property : entity.getProperties()) {
                    SqlTableBuilder.ColumnType columnType;
                    Class<?> propType = property.getType();

                    if (property.isRelationship()) {
                        RelationshipDescription relationship = (RelationshipDescription) property;
                        if (relationship.isToOne()) {
                            columnType = ColumnType.INTEGER;
                        } else {
                            continue;
                        }
                    }
                    else if (int.class.isAssignableFrom(propType) || Integer.class.isAssignableFrom(propType) ||
                            long.class.isAssignableFrom(propType) || Long.class.isAssignableFrom(propType)) {
                        columnType = ColumnType.INTEGER;
                    } else if (String.class.isAssignableFrom(propType) || Enum.class.isAssignableFrom(propType)) {
                        columnType = ColumnType.TEXT;
                    } else if (boolean.class.isAssignableFrom(propType) || Boolean.class.isAssignableFrom(propType)) {
                        columnType = ColumnType.BOOLEAN;
                    } else if (Date.class.isAssignableFrom(property.getType())) {
                        columnType = ColumnType.DATETIME;
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
            metadataValues.put(METADATA_COLUMN_VERSION, m_model.getVersion());
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

    static class SQLiteCursorLoggerFactory implements CursorFactory {

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
