```java
package com.sismics.reader.core.event;

import java.util.List;
import java.util.Objects;

import com.sismics.reader.core.model.dto.NewArticleDto;

/**
 * New articles created event.
 *
 * @author bgamard
 */
public class ArticleCreatedAsyncEvent {

    private final List<NewArticleDto> articles;

    public ArticleCreatedAsyncEvent(List<NewArticleDto> articles) {
        this.articles = articles;
    }

    public List<NewArticleDto> getArticles() {
        return articles;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("articles", (articles != null ? articles.size() : "0") + " articles")
                .toString();
    }
}
```