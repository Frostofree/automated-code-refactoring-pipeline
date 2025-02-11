```java
package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.model.jpa.Category;
import com.sismics.util.context.ThreadLocalContext;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CategoryDao {

    public String create(Category category) {
        category.setId(UUID.randomUUID().toString());
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        category.setCreateDate(new Date());
        em.persist(category);

        return category.getId();
    }

    public Category update(Category category) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Category categoryFromDb = getCategory(category.getId());
        categoryFromDb.setName(category.getName());
        categoryFromDb.setOrder(category.getOrder());
        categoryFromDb.setFolded(category.isFolded());
        
        return categoryFromDb;
    }

    public void delete(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Category categoryFromDb = getCategory(id);
        categoryFromDb.setDeleteDate(new Date());
    }

    public Category getRootCategory(String userId) {
        return getCategory(null, userId);
    }

    public Category getCategory(String id, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select c from Category c where c.id = :id and c.userId = :userId and c.deleteDate is null")
                .setParameter("id", id)
                .setParameter("userId", userId);
        return (Category) q.getSingleResult();
    }

    public List<Category> findAllCategory(String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select c from Category c where c.userId = :userId and c.deleteDate is null order by c.order")
                .setParameter("userId", userId);
        return q.getResultList();
    }

    public List<Category> findSubCategory(String parentId, String userId) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select c from Category c where c.parentId = :parentId and c.userId = :userId and c.deleteDate is null order by c.order")
                .setParameter("parentId", parentId)
                .setParameter("userId", userId);
        return q.getResultList();
    }
}
```