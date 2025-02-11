```java
package com.sismics.reader.core.dao.file.json;

import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.model.jpa.Feed;

/**
 * Event raised on starred articles imported.
 *
 * @author jtremeaux
 */
public class StarredArticleImportedEvent {
    private final Feed feed;
    private final Article article;

    public StarredArticleImportedEvent(Feed feed, Article article) {
        this.feed = feed;
        this.article = article;
    }

    public Feed getFeed() {
        return feed;
    }

    public Article getArticle() {
        return article;
    }

    @Override
    public String toString() {
        return "StarredArticleImportedEvent{" +
                "feed=" + feed +
                ", article=" + article +
                '}';
    }
}
```