```java
package com.sismics.reader.core.constant;

public enum JobConstants {
    IMPORT("import"),
    IMPORT_EVENT_BASE("import."),
    EVENT_FEED_COUNT("feed_count"),
    EVENT_STARRED_ARTICLED_COUNT("starred_article_count"),
    EVENT_FEED_IMPORT_SUCCESS("feed_import_success"),
    EVENT_FEED_IMPORT_FAILURE("feed_import_failure"),
    EVENT_STARRED_ARTICLE_IMPORT_SUCCESS("starred_article_import_success"),
    EVENT_STARRED_ARTICLE_IMPORT_FAILURE("starred_article_import_failure");

    private final String value;

    JobConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
```