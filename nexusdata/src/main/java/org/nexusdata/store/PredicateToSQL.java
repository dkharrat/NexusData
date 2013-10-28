package org.nexusdata.store;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.nexusdata.core.*;
import org.nexusdata.predicate.*;
import org.nexusdata.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class DatabaseQueryService {
    private static final Logger LOG = LoggerFactory.getLogger(DatabaseQueryService.class);

    public static <T extends ManagedObject> Cursor query(SQLiteDatabase db, IncrementalStore store, String tableName, FetchRequest<T> request) {

        LOG.debug("Constructing SQL query for request: " + request);

        String limit = null;
        if (request.getLimit() != Integer.MAX_VALUE || request.getOffset() != 0) {
            limit = String.valueOf(request.getLimit());

            limit += "," + request.getOffset();
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

        QueryString queryString = buildQuery(request.getPredicate());

        String selection = queryString.stringBuilder.length() == 0 ? null : queryString.stringBuilder.toString();
        String[] selectionArgs = queryString.params.isEmpty() ? null : queryString.params.toArray(new String[0]);

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

    private static QueryString buildQuery(Predicate predicate) {
        return new QueryBuilder().visit(predicate);
    }

    static class QueryString {
        final StringBuilder stringBuilder = new StringBuilder();
        final ArrayList<String> params = new ArrayList<String>();
    }

    static class QueryBuilder implements ExpressionVisitor<QueryString> {

        final QueryString queryString = new QueryString();

        @Override
        public QueryString visit(ConstantExpression<?> expression) {
            Object value = expression.getValue();
            if (value.getClass().isAssignableFrom(Boolean.class) || value.getClass().isAssignableFrom(boolean.class)) {
                value = ((Boolean)value) ? "1" : "0";
            }
            queryString.stringBuilder.append(value);
            return queryString;
        }

        @Override
        public QueryString visit(FieldPathExpression expression) {
            queryString.stringBuilder.append(expression.getFieldPath());
            return queryString;
        }

        @Override
        public QueryString visit(CompoundPredicate predicate) {
            String op = null;
            switch(predicate.getOperator()) {
                case AND:   op = " AND "; break;
                case OR:    op = " OR "; break;
            }

            queryString.stringBuilder.append("(");
            visit(predicate.getLhs());
            queryString.stringBuilder.append(op);
            visit(predicate.getRhs());
            queryString.stringBuilder.append(")");

            return queryString;
        }

        @Override
        public QueryString visit(ComparisonPredicate predicate) {
            String op = null;
            switch(predicate.getOperator()) {
                case EQUAL:                 op = " = "; break;
                case GREATER_THAN:          op = " > "; break;
                case GREATER_THAN_OR_EQUAL: op = " >= "; break;
                case LESS_THAN:             op = " < "; break;
                case LESS_THAN_OR_EQUAL:    op = " <= "; break;
                case NOT_EQUAL:             op = " != "; break;
            }

            queryString.stringBuilder.append("(");
            visit(predicate.getLhs());
            queryString.stringBuilder.append(op);
            visit(predicate.getRhs());
            queryString.stringBuilder.append(")");

            return queryString;
        }

        @Override
        public QueryString visit(NotPredicate predicate) {
            queryString.stringBuilder
                    .append("NOT (")
                    .append(visit(predicate.getPredicate()))
                    .append(")");
            return queryString;
        }

        QueryString visit(Predicate predicate) {
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

        QueryString visit(Expression expression) {
            if (expression instanceof ConstantExpression) {
                return visit((ConstantExpression)expression);
            } else if (expression instanceof FieldPathExpression) {
                return visit((FieldPathExpression)expression);
            } else {
                throw new UnsupportedOperationException("Unsupported expression type: " + expression);
            }
        }
    }
}
