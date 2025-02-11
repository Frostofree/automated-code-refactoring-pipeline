```java
package com.sismics.reader.core.dao.jpa;

import com.sismics.reader.core.dao.jpa.criteria.ArticleCriteria;
import com.sismics.reader.core.dao.jpa.dto.ArticleDto;
import com.sismics.reader.core.dao.jpa.mapper.ArticleMapper;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.util.jpa.BaseDao;
import com.sismics.reader.core.util.jpa.SortCriteria;
import com.sismics.util.jpa.CriteriaQueryResult;
import com.sismics.util.jpa.FilterCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ArticleDao extends BaseDao<ArticleDto, ArticleCriteria> {

    @Autowired
    private EntityManager entityManager;

    @Override
    protected CriteriaQueryResult<ArticleDto> find(ArticleCriteria criteria, FilterCriteria filterCriteria) {
        QueryParam queryParam = getQueryParam(criteria, filterCriteria);
        Query query = entityManager.createNativeQuery(queryParam.getQueryString(), ArticleDto.class);
        setQueryParams(queryParam, query);
        return getQueryResult(query, queryParam.getMapper(), filterCriteria);
    }

    private void setQueryParams(QueryParam queryParam, Query query) {
        for (String key : queryParam.getParameterMap().keySet()) {
            query.setParameter(key, queryParam.getParameterMap().get(key));
        }
    }

    // ... Other methods removed for brevity

}

class ArticleDaoImpl implements ArticleDaoCustom {

    @Autowired
    private ArticleDao articleJpaRepository;

    @Override
    public Article create(Article article) {
        return articleJpaRepository.save(article);
    }

    @Override
    public Article update(Article article) {
        return articleJpaRepository.save(article);
    }

    // ... Other methods removed for brevity

}
```