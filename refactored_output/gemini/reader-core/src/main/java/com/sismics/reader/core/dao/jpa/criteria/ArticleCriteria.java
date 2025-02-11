```java
package com.sismics.reader.core.dao.jpa.criteria;

import java.util.Date;

/**
 * Article ID criteria.
 */
public class ArticleIdCriteria {
    private String id;

    public String getId() {
        return id;
    }

    public ArticleIdCriteria setId(String id) {
        this.id = id;
        return this;
    }
}

/**
 * Article GUID criteria.
 */
public class ArticleGuidCriteria {
    private List<String> guids;

    public List<String> getGuids() {
        return guids;
    }

    public ArticleGuidCriteria setGuids(List<String> guids) {
        this.guids = guids;
        return this;
    }
}

/**
 * Article title criteria.
 */
public class ArticleTitleCriteria {
    private String title;

    public String getTitle() {
        return title;
    }

    public ArticleTitleCriteria setTitle(String title) {
        this.title = title;
        return this;
    }
}

/**
 * Article URL criteria.
 */
public class ArticleUrlCriteria {
    private String url;

    public String getUrl() {
        return url;
    }

    public ArticleUrlCriteria setUrl(String url) {
        this.url = url;
        return this;
    }
}

/**
 * Article publication date criteria.
 */
public class ArticlePublicationDateCriteria {
    private Date publicationDateMin;

    public Date getPublicationDateMin() {
        return publicationDateMin;
    }

    public ArticlePublicationDateCriteria setPublicationDateMin(Date publicationDateMin) {
        this.publicationDateMin = publicationDateMin;
        return this;
    }
}

/**
 * Article Feed ID criteria.
 */
public class ArticleFeedIdCriteria {
    private String feedId;

    public String getFeedId() {
        return feedId;
    }

    public ArticleFeedIdCriteria setFeedId(String feedId) {
        this.feedId = feedId;
        return this;
    }
}
```