```java
package com.sismics.reader.core.model.jpa;

import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sismics.reader.core.model.UserArticlePK;

/**
 * Subscription from a user to an article.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_USER_ARTICLE")
public class UserArticle {
    @EmbeddedId
    private UserArticlePK pk;

    private Date createDate;
    
    private Date readDate;

    /**
     * Returns true if the article is starred by the user.
     * @return true if starred, false otherwise
     */
    public boolean isStarred() {
        return starredDate != null;
    }
    
    /**
     * Stars the article for the user.
     */
    public void star() {
        starredDate = new Date();
    }
    
    /**
     * Unstars the article for the user.
     */
    public void unstar() {
        starredDate = null;
    }
    
    /**
     * Returns true if the article is deleted by the user.
     * @return true if deleted, false otherwise
     */
    public boolean isDeleted() {
        return deleteDate != null;
    }
    
    /**
     * Deletes the article for the user.
     */
    public void delete() {
        deleteDate = new Date();
    }

    // Getters and setters for createDate and readDate omitted for brevity
}
```