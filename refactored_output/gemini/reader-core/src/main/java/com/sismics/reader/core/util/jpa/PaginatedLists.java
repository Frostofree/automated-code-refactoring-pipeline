```java
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.filter.FilterColumn;
import com.sismics.util.jpa.sort.SortCriteria;
import com.sismics.util.query.QueryParam;
import com.sismics.util.query.QueryBuilder;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * Utilities for paginated lists.
 *
 * @author jtremeaux
 */
public class PaginatedLists {

    /**
     * Default size of a page.
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * Maximum size of a page.
     */
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Query builder for paginated queries.
     *
     * @param queryParam Query parameters
     * @param <E>        Entity type
     * @return Query builder
     */
    public static <E> QueryBuilder<E> create(QueryParam queryParam) {
        return new QueryBuilder<>(queryParam, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    /**
     * Executes a paginated query.
     *
     * @param paginatedList Paginated list object containing parameters, and into which results are added by side effects
     * @param queryParam    Query parameters
     * @param <E>           Entity type
     */
    public static <E> void execute(PaginatedList<E> paginatedList, QueryParam queryParam) {
        QueryBuilder<E> queryBuilder = create(queryParam);
        paginatedList.setResultCount(queryBuilder.count());
        paginatedList.setResultList(queryBuilder.list());
    }

    /**
     * Query builder for non-paginated queries.
     *
     * @param queryParam Query parameters
     * @param <E>        Entity type
     * @return Query builder
     */
    public static <E> QueryBuilder<E> createNonPaginated(QueryParam queryParam) {
        return new QueryBuilder<>(queryParam, null, null);
    }

    /**
     * Executes a non-paginated query.
     *
     * @param queryParam Query parameters
     * @param <E>        Entity type
     * @return List of results
     */
    public static <E> List<E> executeNonPaginated(QueryParam queryParam) {
        return createNonPaginated(queryParam).list();
    }

    /**
     * Query builder for count(*) queries.
     *
     * @param queryBuilder Query builder
     * @return Query builder
     */
    public static QueryBuilder<Long> createCount(QueryBuilder<?> queryBuilder) {
        return new QueryBuilder<>(queryBuilder.getQueryParam(), null, null, "count(*) as result_count");
    }

    /**
     * Executes a count(*) query.
     *
     * @param queryBuilder Query builder
     * @return Count
     */
    public static Long executeCount(QueryBuilder<?> queryBuilder) {
        return createCount(queryBuilder).count();
    }

    /**
     * Query builder class.
     *
     * @param <E> Entity type
     */
    private static class QueryBuilder<E> {

        private final QueryParam queryParam;

        private final Integer limit;
        private final Integer offset;

        private final String select;

        public QueryBuilder(QueryParam queryParam, Integer limit, Integer offset) {
            this(queryParam, limit, offset, null);
        }

        public QueryBuilder(QueryParam queryParam, Integer limit, Integer offset, String select) {
            this.queryParam = queryParam;
            this.limit = limit;
            this.offset = offset;
            this.select = select;
        }

        public <F> QueryBuilder<F> select(Class<F> clazz) {
            return new QueryBuilder<>(queryParam, limit, offset, queryParam.getQueryString(clazz));
        }

        public Integer count() {
            return executeQuery(createCount(this));
        }

        public List<E> list() {
            return executeQuery(this);
        }

        private <F> List<F> executeQuery(QueryBuilder<F> queryBuilder) {
            StringBuilder sb = new StringBuilder(queryBuilder.select);
            mapQueryParam(sb);
            return executeQuery(sb.toString());
        }

        private void mapQueryParam(StringBuilder sb) {
            if (queryParam.hasCriteria()) {
                List<String> whereList = Lists.newLinkedList(queryParam.getCriteriaList());
                if (queryParam.hasFilterCriteria()) {
                    for (FilterColumn filterColumn : queryParam.getFilterCriteria().getFilterColumnList()) {
                        whereList.add(filterColumn.getPredicate());
                    }
                }
                if (!whereList.isEmpty()) {
                    sb.append(" where ");
                    sb.append(Joiner.on(" and ").join(whereList));
                }
            }
            if (queryParam.hasGroupBy()) {
                sb.append(" group by ");
                sb.append(Joiner.on(", ").join(queryParam.getGroupByList()));
            }
            if (queryBuilder.hasSortCriteria()) {
                sb.append(" order by ");
                sb.append(queryParam.getOrderByClause());
            }
            if (queryBuilder.limit != null && queryBuilder.offset != null) {
                sb.append(" limit ");
                sb.append(queryBuilder.limit);
                sb.append(" offset ");
                sb.append(queryBuilder.offset);
            }
        }

        private boolean hasSortCriteria() {
            return queryParam.hasSortCriteria();
        }

        private List<F> executeQuery(String query) {
            EntityManager em = ThreadLocalContext.get().getEntityManager();
            Query q = em.createNativeQuery(query);
            mapQueryParam(q);
            mapFilterColumn(q);
            return q.getResultList();
        }

        private void mapQueryParam(Query query) {
            for (Map.Entry<String, Object> parameter : queryParam.getParameterMap().entrySet()) {
                query.setParameter(parameter.getKey(), parameter.getValue());
            }
        }

        private void mapFilterColumn(Query query) {
            if (queryParam.hasFilterCriteria()) {
                for (FilterColumn filterColumn : queryParam.getFilterCriteria().getFilterColumnList()) {
                    if (filterColumn.hasParam()) {
                        query.setParameter(filterColumn.getParamName(), filterColumn.getParamValue());
                    }
                }
            }
        }
    }
}
```