```java
package com.sismics.reader.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.reader.core.event.RebuildIndexAsyncEvent;
import com.sismics.reader.core.model.jpa.Article;
import com.sismics.reader.core.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Listener on rebuild index.
 *
 * @author bgamard
 */
public class RebuildIndexAsyncListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(RebuildIndexAsyncListener.class);

    /**
     * Index service.
     */
    private final IndexService indexService = new IndexService();

    /**
     * Rebuild articles index.
     *
     * @param rebuildIndexAsyncEvent Index rebuild event
     */
    @Subscribe
    public void onArticleCreated(final RebuildIndexAsyncEvent rebuildIndexAsyncEvent) {
        if (log.isInfoEnabled()) {
            log.info("Rebuild index event: " + rebuildIndexAsyncEvent.toString());
        }

        // Fetch all articles
        List<Article> articleList = indexService.getAllArticles();

        // Rebuild index
        indexService.rebuildIndex(articleList);
    }
}
```