```java
package com.sismics.reader.core.dao.file.json;

import com.sismics.reader.core.model.ImportedArticle;

/**
 * Listener for starred item import events.
 *
 * @author jtremeaux 
 */
public interface StarredArticleImportedListener {
    /**
     * Invoked when a new starred item is imported.
     * 
     * @param importedArticle Imported article
     */
    void onStarredArticleImported(ImportedArticle importedArticle);
}
```