package com.github.dkharrat.nexusdata.store;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.github.dkharrat.nexusdata.core.*;
import com.github.dkharrat.nexusdata.predicate.*;
import com.github.dkharrat.nexusdata.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class DatabaseQueryService {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseQueryService.class);

    public static <T extends ManagedObject> Cursor query(SQLiteDatabase db, final IncrementalStore store, String tableName, FetchRequest<T> request) {

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
                orderBys.add(sortDesc.getAttributeName() + orderType);
            }

            orderBy = StringUtil.join(orderBys, ",");
        }


        String selection = null;
        String[] selectionArgs = null;
        if (request.getPredicate() != null) {
            QueryParts queryParts = buildQuery(store, request.getPredicate());

            selection = queryParts.stringBuilder.toString();
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

    private static QueryParts buildQuery(IncrementalStore store, Predicate predicate) {
        return new QueryBuilder(store).visit(predicate);
    }

    private static class QueryParts {
        private final StringBuilder stringBuilder = new StringBuilder();
        private final ArrayList<String> params = new ArrayList<String>();
    }

    private static class QueryBuilder implements ExpressionVisitor<QueryParts> {

        final IncrementalStore store;
        final QueryParts queryParts = new QueryParts();

        QueryBuilder(IncrementalStore store) {
            this.store = store;
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
            queryParts.stringBuilder.append(expression.getFieldPath());
            return queryParts;
        }

        @Override
        public QueryParts visit(ThisExpression expression) {
            queryParts.stringBuilder.append(AndroidSqlPersistentStore.COLUMN_ID_NAME);
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
