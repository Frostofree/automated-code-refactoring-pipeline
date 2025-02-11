```java
package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.model.jpa.FeedSynchronization;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.DialectUtil;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Feed synchronization DAO.
 */
public class FeedSynchronizationDao {

    public String create(FeedSynchronization feedSynchronization) {
        feedSynchronization.setId(UUID.randomUUID().toString());
        feedSynchronization.setCreateDate(new Date());
        ThreadLocalContext.get().getEntityManager().persist(feedSynchronization);
        return feedSynchronization.getId();
    }

    public void deleteOldFeedSynchronizations(String feedId, int minutes) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.createNativeQuery("delete from T_FEED_SYNCHRONIZATION fs " +
                "where FSY_IDFEED_C = :feedId " +
                "and FSY_CREATEDATE_D < " +
                DialectUtil.getDateDiff("NOW()", String.valueOf(minutes), "MINUTE"))
                .setParameter("feedId", feedId)
                .executeUpdate();
    }

    public List<FeedSynchronization> findByFeedId(String feedId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.createQuery("select fs from FeedSynchronization fs where fs.feedId = :feedId order by fs.createDate desc")
                .setParameter("feedId", feedId)
                .getResultList();
    }
}
```