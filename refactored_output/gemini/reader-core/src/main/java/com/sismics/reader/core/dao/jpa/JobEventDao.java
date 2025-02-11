**Refactored Java Code:**

```java
package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.dto.JobEventDto;
import com.sismics.reader.core.dao.jpa.mapper.JobEventMapper;
import com.sismics.reader.core.model.jpa.JobEvent;
import com.sismics.util.jpa.BaseDao;
import com.sismics.util.jpa.QueryParam;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.UUID;

public class JobEventDao extends BaseDao<JobEventDto, JobEvent> {

    public JobEventDao(EntityManager em) {
        super(em, JobEvent.class, new JobEventMapper());
    }

    public String createOrUpdate(JobEvent jobEvent) {
        jobEvent.setUpdateDate(new Date());
        if (jobEvent.getId() == null) {
            jobEvent.setId(UUID.randomUUID().toString());
            jobEvent.setCreateDate(new Date());
        }
        save(jobEvent);
        return jobEvent.getId();
    }

    public void delete(String id) {
        JobEvent jobEvent = findById(id);
        jobEvent.setDeleteDate(new Date());
        save(jobEvent);
    }

    @Override
    protected QueryParam getQueryParam(JobEvent criteria) {
        return new QueryParam("select e.id, e.name, e.value from JobEvent e where e.deleteDate is null", null, null, new SortCriteria("order by e.createDate asc"));
    }
}
```