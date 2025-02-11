```java
public interface FeedSynchronizationService {
    void synchronizeAllFeeds();
    Feed synchronize(String url);
}

public interface ArticleUpdateService {
    void updateArticles(List<ArticleDto> currentArticleDtoList, Map<String, Article> articleMap);
}
```