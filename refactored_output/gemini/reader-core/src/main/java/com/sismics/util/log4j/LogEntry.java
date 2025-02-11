```java
package com.sismics.util.log4j;

import java.time.Instant;

/**
 * A log entry.
 */
public class LogEntry {
    private final Instant timestamp;
    private final String level;
    private final String tag;
    private final String message;

    private LogEntry(Instant timestamp, String level, String tag, String message) {
        this.timestamp = timestamp;
        this.level = level;
        this.tag = tag;
        this.message = message;
    }

    public static LogEntry create(Instant timestamp, String level, String tag, String message) {
        return new LogEntry(timestamp, level, tag, message);
    }

    // Getters with immutable copies to avoid data corruption
    public Instant getTimestamp() {
        return Instant.from(timestamp);
    }

    public String getLevel() {
        return level;
    }

    public String getTag() {
        return tag;
    }

    public String getMessage() {
        return message;
    }
}
```