```java
import com.google.common.collect.Lists;
import com.sismics.reader.core.dao.jpa.criteria.FilterCriteria;
import com.sismics.reader.core.dao.jpa.criteria.SortCriteria;
import com.sismics.reader.core.dao.jpa.dto.FeedSubscriptionDto;
import com.sismics.reader.core.dao.jpa.mapper.FeedSubscriptionMapper;
import com.sismics.reader.core.model.jpa.FeedSubscription;
import com.sismics.reader.core.util.jpa.QueryParam;
import com.sismics.reader.core.util.jpa.TransactionalDao;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.BaseDao;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "T_FEED_SUBSCRIPTION")
public class FeedSubscriptionDao extends BaseDao<FeedSubscriptionDto, FilterCriteria> {

    public static final String ID_COLUMN = "FES_ID_C";

    @TransactionalDao
    @Id
    @Column(name = ID_COLUMN)
    private String id;

    @Column(name = "FES_TITLE_C")
    private String title;

    @Column(name = "FES_UNREADCOUNT_N")
    private int unreadCount;

    @Column(name = "FES_CREATEDATE_D")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    @Column(name = "FES_DELETPDATE_D")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deleteDate;

    @Column(name = "FES_IDUSER_C")
    private String userId;

    @Column(name = "FES_IDFEED_C")
    private String feedId;

    @Column(name = "FES_ORDER_N")
    private int order;

    @Column(name = "FES_IDCATEGORY_C")
    private String categoryId;

    @Override
    protected QueryParam getQueryParam(FilterCriteria filterCriteria) {
        List<String> criteriaList = Lists.newArrayList();
        Map<String, Object> parameterMap = new HashMap<>();

        StringBuilder sb = new StringBuilder("select fs.FES_ID_C, fs.FES_TITLE_C, fs.FES_UNREADCOUNT_N, fs.FES_CREATEDATE_D, fs.FES_IDUSER_C, f.FED_ID_C, f.FED_TITLE_C, f.FED_RSSURL_C, f.FED_URL_C, f.FED_DESCRIPTION_C, c.CAT_ID_C, c.CAT_IDPARENT_C, c.CAT_NAME_C, c.CAT_FOLDED_B")
                .append(" from T_FEED_SUBSCRIPTION fs ")
                .append(" join T_FEED f on(f.FED_ID_C = fs.FES_IDFEED_C and f.FED_DELETEDATE_D is null) ")
                .append(" join T_CATEGORY c on(c.CAT_ID_C = fs.FES_IDCATEGORY_C and c.CAT_DELETEDATE_D is null) ");

        // Adds search criteria
        criteriaList.add("fs.FES_DELETEDATE_D is null");

        SortCriteria sortCriteria = new SortCriteria("  order by c.CAT_IDPARENT_C asc, c.CAT_ORDER_N asc, fs.FES_ORDER_N asc");

        return new QueryParam(sb.toString(), criteriaList, parameterMap, sortCriteria, filterCriteria, new FeedSubscriptionMapper());
    }

    public static FeedSubscription create(FeedSubscription feedSubscription) {
        // Create the UUID
        feedSubscription.setId(UUID.randomUUID().toString());

        FeedSubscriptionDao dao = ThreadLocalContext.get().getDao(FeedSubscriptionDao.class);
        feedSubscription.setCreateDate(new Date());
        dao.save(feedSubscription);

        return feedSubscription;
    }

    public FeedSubscription update(FeedSubscription feedSubscription) {
        feedSubscription.setUpdateDate(new Date());
        return save(feedSubscription);
    }

    public void updateUnreadCount(String id, Integer unreadCount) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createNativeQuery("update T_FEED_SUBSCRIPTION set FES_UNREADCOUNT_N = :unreadCount where FES_ID_C = :id")
                .setParameter("id", id)
                .setParameter("unreadCount", unreadCount)
                .executeUpdate();
    }

    public void reorder(FeedSubscription feedSubscription, int order) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Find categories with the same parent
        Query q = em.createQuery("select fs from FeedSubscription fs where fs.categoryId = :categoryId and fs.userId = :userId and fs.deleteDate is null order by fs.order")
                .setParameter("categoryId", feedSubscription.getCategoryId())
                .setParameter("userId", feedSubscription.getUserId());
        List<FeedSubscription> feedSubscriptionList = (List<FeedSubscription>) q.getResultList();
        for (int i = 0; i < feedSubscriptionList.size(); i++) {
            FeedSubscription currentFeedSubscription = feedSubscriptionList.get(i);
            if (currentFeedSubscription.getId().equals(feedSubscription.getId())) {
                feedSubscriptionList.remove(i);
            }
        }
        feedSubscriptionList.add(order > feedSubscriptionList.size() ? feedSubscriptionList.size() : order, feedSubscription);

        int i = 0;
        for (FeedSubscription fs : feedSubscriptionList) {
            fs.setOrder(i);
            save(fs);
            i++;
        }
    }

    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createQuery("update FeedSubscription fs set fs.deleteDate = :deleteDate where fs.id = :id")
                .setParameter("id", id)
                .setParameter("deleteDate", new Date())
                .executeUpdate();
    }
}
```