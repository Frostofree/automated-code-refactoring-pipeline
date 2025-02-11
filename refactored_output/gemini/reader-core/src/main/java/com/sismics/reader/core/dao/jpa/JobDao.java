```java
// JobDao.java
import com.sismics.reader.core.dao.jpa.criteria.JobCriteria;
import com.sismics.reader.core.dao.jpa.dto.JobDto;
import com.sismics.reader.core.dao.jpa.mapper.JobMapper;
import com.sismics.reader.core.model.jpa.Job;
import com.sismics.reader.core.util.jpa.BaseDao;
import com.sismics.reader.core.util.jpa.QueryParam;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.jpa.filter.FilterCriteria;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.*;

public class JobDao extends BaseDao<JobDto, JobCriteria> {

    @Override
    protected QueryParam getQueryParam(JobCriteria criteria, FilterCriteria filterCriteria) {
        return new QueryParam("select j from Job j", null, null, null, filterCriteria, new JobMapper());
    }
}

// JobCrudDao.java
import com.sismics.reader.core.model.jpa.Job;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.Date;

class JobCrudDao {

    private final EntityManager em = ThreadLocalContext.get().getEntityManager();

    public Job create(Job job) {
        em.persist(job);
        return job;
    }

    public Job getActiveJob(String id) {
        try {
            return (Job) em.createQuery("select j from Job j where j.id = :id and j.deleteDate is null")
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void delete(String id) {
        Job jobFromDb = getActiveJob(id);
        if (jobFromDb != null) {
            jobFromDb.setDeleteDate(new Date());
        }
    }

    public Job update(Job job) {
        Job jobFromDb = getActiveJob(job.getId());
        if (jobFromDb != null) {
            jobFromDb.setStartDate(job.getStartDate());
            jobFromDb.setEndDate(job.getEndDate());
        }
        return job;
    }
}

// JobService.java
import com.sismics.reader.core.model.jpa.Job;

class JobService {

    private final JobCrudDao jobCrudDao;

    public JobService(JobCrudDao jobCrudDao) {
        this.jobCrudDao = jobCrudDao;
    }

    public String create(Job job) {
        job.setId(UUID.randomUUID().toString());
        job.setCreateDate(new Date());
        return jobCrudDao.create(job).getId();
    }

    public Job getActiveJob(String id) {
        return jobCrudDao.getActiveJob(id);
    }

    public void delete(String id) {
        jobCrudDao.delete(id);
    }

    public Job update(Job job) {
        return jobCrudDao.update(job);
    }
}
```