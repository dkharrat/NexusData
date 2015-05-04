package com.github.dkharrat.nexusdata.store;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.dkharrat.nexusdata.core.*;
import com.github.dkharrat.nexusdata.metamodel.Entity;
import com.github.dkharrat.nexusdata.predicate.*;
import com.github.dkharrat.nexusdata.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

class DatabaseQueryService {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseQueryService.class);

    public static <T extends ManagedObject> Cursor query(SQLiteDatabase db, final AndroidSqlPersistentStore store, String tableName, FetchRequest<T> request) {

        LOG.debug("Constructing SQL query for request: " + request);

        String limit = null;
        if (request.getLimit() != Integer.MAX_VALUE || request.getOffset() != 0) {
            limit = String.valueOf(request.getOffset()) + "," + request.getLimit();
        }

        String orderBy = null;
        if (request.hasSortDescriptors()) {
            List<String> orderBys = new ArrayList<String>();
            for (SortDescriptor sortDesc : request.getSortDescriptors()) {
                String orderType = sortDesc.isAscending() ? " ASC" : " DESC";
                String columnName = getColumnName(request.getEntity(), store.getEntityToIDMap(), sortDesc.getAttributeName());
                orderBys.add(columnName + orderType);
            }

            orderBy = StringUtil.join(orderBys, ",");
        }


        String selection = getEntityIDsCondition(store, request.getEntity());
        String[] selectionArgs = null;
        if (request.getPredicate() != null) {
            QueryParts queryParts = buildQuery(store, request.getEntity(), request.getPredicate());

            selection += " AND " + queryParts.stringBuilder.toString();
            selectionArgs = queryParts.params.isEmpty() ? null : queryParts.params.toArray(new String[0]);
        }

        Cursor cursor = db.query(
                false,          // not distinct
                tableName,
                null,           // columns,
                selection,
                selectionArgs,  // selectionArgs
                null,           // groupBy
                null,           // having
                orderBy,        // orderBy
                limit);         // limit

        return cursor;
    }

    private static String getEntityIDsCondition(final AndroidSqlPersistentStore store, Entity<?> entity) {
        return AndroidSqlPersistentStore.ENTITY_COLUMN_NAME + " IN (" + StringUtil.join(getEntityInheritanceIDs(store, entity), ",") + ")";
    }

    private static Collection<Integer> getEntityInheritanceIDs(final AndroidSqlPersistentStore store, Entity<?> entity) {
        List<Integer> entityIDs = new ArrayList<>();

        entityIDs.add(store.getEntityToIDMap().get(entity));
        for (Entity<?> childEntity : Utils.getAllChildEntities(entity, new ArrayList<Entity<?>>())) {
            entityIDs.add(store.getEntityToIDMap().get(childEntity));
        }

        return entityIDs;
    }

    private static String getColumnName(Entity<?> entity, Map<Entity<?>,Integer> entityIDMap, String fieldName) {
        if (entity.getSuperEntity() == null || !entity.getSuperEntity().hasProperty(fieldName)) {
            return fieldName + "_" + entityIDMap.get(entity);
        } else {
            return getColumnName(entity.getSuperEntity(), entityIDMap, fieldName);
        }
    }

    private static QueryParts buildQuery(AndroidSqlPersistentStore store, Entity<?> entity, Predicate predicate) {
        return new QueryBuilder(store, entity).visit(predicate);
    }

    private static class QueryParts {
        private final StringBuilder stringBuilder = new StringBuilder();
        private final ArrayList<String> params = new ArrayList<String>();
    }

    private static class QueryBuilder implements ExpressionVisitor<QueryParts> {

        final AndroidSqlPersistentStore store;
        final Entity<?> entity;
        final QueryParts queryParts = new QueryParts();

        QueryBuilder(AndroidSqlPersistentStore store, Entity<?> entity) {
            this.store = store;
            this.entity = entity;
        }

        @Override
        public QueryParts visit(ConstantExpression<?> expression) {
            Object value = expression.getValue();
            if (value == null) {
                queryParts.stringBuilder.append("NULL");
            } else {
                if (value instanceof ManagedObject) {
                    ManagedObject relatedObject = (ManagedObject)value;
                    value = store.getReferenceObjectForObjectID(relatedObject.getID()).toString();
                } else if (value.getClass().isAssignableFrom(Boolean.class) || value.getClass().isAssignableFrom(boolean.class)) {
                    value = ((Boolean)value) ? "1" : "0";
                }
                queryParts.stringBuilder.append("?");
                queryParts.params.add(value.toString());
            }

            return queryParts;
        }

        @Override
        public QueryParts visit(FieldPathExpression expression) {
            queryParts.stringBuilder.append(getColumnName(entity, store.getEntityToIDMap(), expression.getFieldPath()));
            return queryParts;
        }

        @Override
        public QueryParts visit(ThisExpression expression) {
            queryParts.stringBuilder.append(AndroidSqlPersistentStore.ID_COLUMN_NAME);
            return queryParts;
        }

        @Override
        public QueryParts visit(CompoundPredicate predicate) {
            String op = null;
            switch(predicate.getOperator()) {
                case AND:   op = " AND "; break;
                case OR:    op = " OR "; break;
            }

            queryParts.stringBuilder.append("(");
            visit(predicate.getLhs());
            queryParts.stringBuilder.append(op);
            visit(predicate.getRhs());
            queryParts.stringBuilder.append(")");

            return queryParts;
        }

        @Override
        public QueryParts visit(ComparisonPredicate predicate) {
            String op = null;
            if (predicate.getRhs() instanceof ConstantExpression<?> && ((ConstantExpression<?>)predicate.getRhs()).getValue() == null) {
                switch(predicate.getOperator()) {
                    case EQUAL:                 op = " IS "; break;
                    case NOT_EQUAL:             op = " IS NOT "; break;
                    default: throw new UnsupportedOperationException("Invalid operator " + op + " with 'null' comparison.");
                }
            } else {
                switch(predicate.getOperator()) {
                    case EQUAL:                 op = " = "; break;
                    case GREATER_THAN:          op = " > "; break;
                    case GREATER_THAN_OR_EQUAL: op = " >= "; break;
                    case LESS_THAN:             op = " < "; break;
                    case LESS_THAN_OR_EQUAL:    op = " <= "; break;
                    case NOT_EQUAL:             op = " != "; break;
                }
            }

            queryParts.stringBuilder.append("(");
            visit(predicate.getLhs());
            queryParts.stringBuilder.append(op);
            visit(predicate.getRhs());
            queryParts.stringBuilder.append(")");

            return queryParts;
        }

        @Override
        public QueryParts visit(NotPredicate predicate) {
            queryParts.stringBuilder
                    .append("NOT (")
                    .append(visit(predicate.getPredicate()))
                    .append(")");
            return queryParts;
        }

        QueryParts visit(Predicate predicate) {
            if (predicate instanceof CompoundPredicate) {
                return visit((CompoundPredicate)predicate);
            } else if (predicate instanceof ComparisonPredicate) {
                return visit((ComparisonPredicate)predicate);
            } else if (predicate instanceof NotPredicate) {
                return visit((NotPredicate)predicate);
            } else {
                throw new UnsupportedOperationException("Unsupported predicate type: " + predicate);
            }
        }

        QueryParts visit(Expression expression) {
            if (expression instanceof ConstantExpression) {
                return visit((ConstantExpression)expression);
            } else if (expression instanceof FieldPathExpression) {
                return visit((FieldPathExpression)expression);
            } else if (expression instanceof ThisExpression) {
                return visit((ThisExpression)expression);
            } else {
                throw new UnsupportedOperationException("Unsupported expression type: " + expression);
            }
        }
    }
}
