```java
package com.sismics.reader.core.listener.async;

import com.google.common.eventbus.Subscribe;
import com.sismics.reader.core.event.ArticleDeletedAsyncEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deletes the index of deleted articles.
 *
 * @author bgamard
 */
public class ArticleDeletedIndexListener {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(ArticleDeletedIndexListener.class);

    /**
     * Process deleted articles.
     *
     * @param articlesDeletedAsyncEvent Deleted articles event
     */
    @Subscribe
    public void onArticleDeleted(final ArticleDeletedAsyncEvent articlesDeletedAsyncEvent) throws Exception {
        long startTime = System.currentTimeMillis();

        // Delete index
        articlesDeletedAsyncEvent.deleteIndex();

        long endTime = System.currentTimeMillis();
        if (log.isInfoEnabled()) {
            log.info(MessageFormat.format("Index deleted in {0}ms", endTime - startTime));
        }
    }
}
```