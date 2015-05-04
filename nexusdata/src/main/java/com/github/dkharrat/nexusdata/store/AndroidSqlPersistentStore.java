package com.github.dkharrat.nexusdata.store;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.github.dkharrat.nexusdata.core.*;
import com.github.dkharrat.nexusdata.metamodel.*;
import com.github.dkharrat.nexusdata.utils.android.CursorUtil;
import com.github.dkharrat.nexusdata.utils.DateUtil;

/* TODO: AndroidSqlPersistentStore changes
 *  - improve memory-management
 *  - code clean-up (see todo's in code below)
 */

public class AndroidSqlPersistentStore extends IncrementalStore {

    private static final Logger LOG = LoggerFactory.getLogger(AndroidSqlPersistentStore.class);

    static final String ID_COLUMN_NAME = "_ID";
    static final String ENTITY_COLUMN_NAME = "_ENT";

    private DatabaseHelper databaseHelper;
    private Map<Entity<?>,Integer> entityToIDMap = new HashMap<>();
    private Map<Integer,Entity<?>> idToEntityMap = new HashMap<>();
    private Map<String,Long> lastRowIDs = new HashMap<>();
    private Context context;

    private SQLiteDatabase db;

    // TODO: use a MRU cache and also remove objects if they are unregistered from all contexts
    private Map<Class<?>, Map<Long,StoreCacheNode>> cache = new HashMap<Class<?>, Map<Long,StoreCacheNode>>();

    public AndroidSqlPersistentStore(Context context, URL path) {
        super(path);
        this.context = context;
    }

    public AndroidSqlPersistentStore(Context context, File location) {
        super(location);
        this.context = context;
    }

    @Override
    protected void loadMetadata() {
        ObjectModel model = getCoordinator().getModel();
        databaseHelper = new DatabaseHelper(context, new File(getLocation().getPath()), model);

        // TODO: does the DB need to be closed at some point?
        db = databaseHelper.getWritableDatabase();
        entityToIDMap = databaseHelper.getEntityIDs(db);
        idToEntityMap = new HashMap<>();

        for(Map.Entry<Entity<?>,Integer> entry : entityToIDMap.entrySet()){
            idToEntityMap.put(entry.getValue(), entry.getKey());
        }

        setUuid(DatabaseHelper.getDatabaseUuid(db, model.getVersion()));
    }

    Map<Entity<?>,Integer> getEntityToIDMap() {
        return entityToIDMap;
    }

    protected String getColumnName(Property property) {
        return property.getName() + "_" + entityToIDMap.get(property.getEntity());
    }

    protected String getQuotedColumnName(Property property) {
        return "`" + getColumnName(property) + "`";
    }

    private ManagedObject createObjectFromCursor(ObjectContext context, Cursor cursor) {

        long id = CursorUtil.getLong(cursor, ID_COLUMN_NAME);
        Entity<?> entity = idToEntityMap.get(CursorUtil.getInt(cursor, ENTITY_COLUMN_NAME));
        ObjectID objectID = this.createObjectID(entity, id);
        ManagedObject object = context.objectWithID(objectID);

        StoreCacheNode cacheNode = getStoreNodeFromCursor(objectID, cursor, context);
        Map<Long,StoreCacheNode> entityCache = cache.get(entity.getType());
        if (entityCache == null) {
            entityCache = new HashMap<>();
            cache.put(entity.getType(), entityCache);
        }
        entityCache.put(id, cacheNode);

        return object;
    }

    @Override
    protected <T extends ManagedObject> List<T> executeFetchRequest(FetchRequest<T> request, ObjectContext context) {
        Cursor cursor = DatabaseQueryService.query(db, this, DatabaseHelper.getTableName(request.getEntity()), request);

        List<T> results = new ArrayList<T>();
        while(cursor.moveToNext()) {
            @SuppressWarnings("unchecked")
            T object = (T)createObjectFromCursor(context, cursor);
            results.add(object);
        }

        cursor.close();

        return results;
    }

    private ContentValues getContentValues(ManagedObject object) throws IllegalArgumentException, IllegalAccessException {
        ContentValues values = new ContentValues();

        values.put(ID_COLUMN_NAME, getReferenceObjectForObjectID(object.getID()).toString());
        for (Property property : object.getEntity().getProperties()) {
            Class<?> propertyType = property.getType();
            Object value = object.getValue(property.getName());

            if (property.isRelationship()) {
                Relationship relationship = (Relationship)property;
                if (relationship.isToOne()) {
                    ManagedObject toOneObject = (ManagedObject) value;
                    if (toOneObject != null) {
                        values.put(getQuotedColumnName(relationship), getReferenceObjectForObjectID(toOneObject.getID()).toString());
                    } else {
                        values.putNull(getQuotedColumnName(relationship));
                    }
                }
            } else {
                if (value != null) {
                    if (Date.class.isAssignableFrom(propertyType)) {
                        values.put(getQuotedColumnName(property), DateUtil.format(DateUtil.ISO8601_NO_TIMEZONE, (Date)value));
                    } else if (Boolean.class.isAssignableFrom(propertyType)) {
                        values.put(getQuotedColumnName(property), ((Boolean)value) ? "1" : "0" );
                    } else {
                        values.put(getQuotedColumnName(property), value.toString());
                    }
                } else {
                    values.putNull(getQuotedColumnName(property));
                }
            }
        }

        return values;
    }

    @Override
    protected void executeSaveRequest(SaveChangesRequest request, ObjectContext context) {
        db.beginTransaction();
        try {
            for (ManagedObject object : request.getChanges().getInsertedObjects()) {
                ContentValues values = getContentValues(object);
                values.put(ENTITY_COLUMN_NAME, entityToIDMap.get(object.getEntity()));
                //TODO: log inserts, updates & deletes
                db.insertOrThrow(DatabaseHelper.getTableName(object.getEntity()), null, values);
            }

            for (ManagedObject object : request.getChanges().getUpdatedObjects()) {
                ContentValues values = getContentValues(object);
                long id = (Long)getReferenceObjectForObjectID(object.getID());
                db.update(DatabaseHelper.getTableName(object.getEntity()), values, "_ID = " + id, null);

                Map<Long, StoreCacheNode> entityCache = cache.get(object.getEntity().getType());
                if (entityCache != null) {
                    //TODO: update cache entry instead of deleting it
                    entityCache.remove(id);
                }
            }

            for (ManagedObject object : request.getChanges().getDeletedObjects()) {
                long id = (Long)getReferenceObjectForObjectID(object.getID());
                db.delete(DatabaseHelper.getTableName(object.getEntity()), "_ID = " + id, null);

                Map<Long, StoreCacheNode> entityCache = cache.get(object.getEntity().getType());
                if (entityCache != null) {
                    entityCache.remove(id);
                }
            }

            db.setTransactionSuccessful();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            db.endTransaction();
        }
    }

    private StoreCacheNode getStoreNodeFromCursor(ObjectID objectID, Cursor cursor, ObjectContext context) {
        StoreCacheNode node = new StoreCacheNode(objectID);

        try {
            for (Property property : objectID.getEntity().getProperties()) {
                Object value;
                @SuppressWarnings("unchecked")
                Class<?> propType = property.getType();

                String columnName = getColumnName(property);

                if (property.isRelationship()) {
                    Relationship relationship = (Relationship)property;
                    if (relationship.isToOne()) {
                        @SuppressWarnings("unchecked")
                        Entity<?> assocEntity = getCoordinator().getModel().getEntity((Class<ManagedObject>)relationship.getType());
                        long relatedID = CursorUtil.getLong(cursor, columnName);
                        if (relatedID != 0) {
                            value = this.createObjectID(assocEntity, relatedID);
                        } else {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else if (CursorUtil.isNull(cursor, columnName)) {
                    value = null;
                } else if (propType.isAssignableFrom(Integer.class) || propType.isAssignableFrom(int.class)) {
                    value = CursorUtil.getInt(cursor, columnName);
                } else if (propType.isAssignableFrom(Long.class) || propType.isAssignableFrom(long.class)) {
                    value = CursorUtil.getLong(cursor, columnName);
                } else if (propType.isAssignableFrom(String.class)) {
                    value = CursorUtil.getString(cursor, columnName);
                } else if (propType.isAssignableFrom(Boolean.class) || propType.isAssignableFrom(boolean.class)) {
                    value = CursorUtil.getBoolean(cursor, columnName);
                } else if (propType.isAssignableFrom(Float.class) || propType.isAssignableFrom(float.class)) {
                    value = CursorUtil.getFloat(cursor, columnName);
                } else if (propType.isAssignableFrom(Double.class) || propType.isAssignableFrom(double.class)) {
                    value = CursorUtil.getDouble(cursor, columnName);
                } else if (Enum.class.isAssignableFrom(propType)) {
                    String enumName = CursorUtil.getString(cursor, columnName);
                    if (enumName != null) {
                        value = Enum.valueOf((Class<? extends Enum>)propType, enumName);
                    } else {
                        value = null;
                    }
                } else if (propType.isAssignableFrom(Date.class)) {
                    String dateStr = CursorUtil.getString(cursor, columnName);
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
        } catch (IllegalArgumentException | ParseException e) {
            throw new RuntimeException(e);
        }

        return node;
    }

    @Override
    protected StoreCacheNode getObjectValues(ObjectID objectID, ObjectContext context) {

        long id = Long.valueOf(getReferenceObjectForObjectID(objectID).toString());

        Map<Long,StoreCacheNode> entityCache = cache.get(objectID.getEntity().getType());
        if (entityCache != null) {
            StoreCacheNode node = entityCache.get(id);
            if (node != null) {
                return node;
            }
        }

        Cursor cursor = db.query(
                false,          // not distinct
                DatabaseHelper.getTableName(objectID.getEntity()),
                null,           // columns
                ID_COLUMN_NAME + "=?",           // selection
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
            cache.put(objectID.getEntity().getType(), entityCache);
        }
        entityCache.put(id, node);

        return node;
    }

    @Override
    protected Set<ObjectID> getToManyRelationshipValue(
            ObjectID objectID,
            Relationship relationship,
            ObjectContext context) {

        String[] columns = new String[]{ID_COLUMN_NAME};
        String table = DatabaseHelper.getTableName(relationship.getDestinationEntity());
        String selection = getQuotedColumnName(relationship.getInverse())+"=?";
        String[] selectionArgs = new String[]{getReferenceObjectForObjectID(objectID).toString()};

        Cursor cursor = db.query(
                false,          // not distinct
                table,
                columns,        // columns
                selection,      // selection
                selectionArgs,  // selectionArgs
                null,           // groupBy
                null,           // having
                null,           // orderBy
                null);          // limit


        Set<ObjectID> results = new HashSet<ObjectID>();
        while(cursor.moveToNext()) {
            long id = CursorUtil.getLong(cursor, ID_COLUMN_NAME);
            ObjectID relatedObject = this.createObjectID(relationship.getDestinationEntity(), id);
            results.add(relatedObject);
        }
        cursor.close();

        return results;
    }

    @Override
    protected ObjectID getToOneRelationshipValue(
            ObjectID objectID,
            Relationship relationship,
            ObjectContext context) {

        String fromTable = DatabaseHelper.getTableName(objectID.getEntity());
        String toTable = DatabaseHelper.getTableName(relationship.getDestinationEntity());

        String table = fromTable + " t1," + toTable + " t2";
        String[] columns = new String[]{"t1" + "." + ID_COLUMN_NAME};
        String selection = "t1"+"."+ ID_COLUMN_NAME +"="+getReferenceObjectForObjectID(objectID) + " AND " +
                           "t1"+"."+ getQuotedColumnName(relationship)+"=t2."+ ID_COLUMN_NAME;

        Cursor cursor = db.query(
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
            long id = CursorUtil.getLong(cursor, ID_COLUMN_NAME);
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

            String tableName = DatabaseHelper.getTableName(object.getEntity());
            Long lastRowID = lastRowIDs.get(tableName);
            if (lastRowID == null) {
                lastRowID = getLastRowIDFromDatabase(db, tableName);
            }
            id = createObjectID(object.getEntity(), lastRowID++);
            lastRowIDs.put(DatabaseHelper.getTableName(object.getEntity()), lastRowID);

            objectIDs.add(id);
        }

        return objectIDs;
    }
}
