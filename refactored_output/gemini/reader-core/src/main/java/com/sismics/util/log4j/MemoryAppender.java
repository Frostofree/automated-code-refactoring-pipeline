```java
package com.sismics.util.log4j;

import com.google.common.collect.Lists;
import com.sismics.reader.core.util.jpa.PaginatedList;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * MemoryAppender skeleton.
 *
 * @author jtremeaux
 */
public class MemoryAppender extends AppenderSkeleton {

    private final LoggingQueue queue;

    public MemoryAppender(int size) {
        this.queue = new InternalLoggingQueue(size);
        this.setSize(size);
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
    }

    @Override
    public synchronized void append(LoggingEvent event) {
        if (closed) {
            LogLog.warn("This appender is already closed, cannot append event.");
            return;
        }

        LogEntry logEntry = new LogEntry(System.currentTimeMillis(), event.getLevel().toString(), event.getLoggerName(), event.getMessage().toString());
        queue.add(logEntry);
    }

    public Queue<LogEntry> getLogList() {
        return queue.toList();
    }

    public void setSize(int size) {
        queue.setMaxSize(size);
    }

    public interface LoggingQueue {
        void add(LogEntry logEntry);
        List<LogEntry> toList();
        void setMaxSize(int size);
    }

    private static class InternalLoggingQueue implements LoggingQueue {

        private final ConcurrentLinkedQueue<LogEntry> logs;
        private int maxSize;

        public InternalLoggingQueue(int maxSize) {
            this.logs = new ConcurrentLinkedQueue<>();
            setMaxSize(maxSize);
        }

        @Override
        public void add(LogEntry logEntry) {
            logs.add(logEntry);
            if (logs.size() > maxSize) {
                logs.poll();
            }
        }

        @Override
        public List<LogEntry> toList() {
            return Lists.newArrayList(logs);
        }

        @Override
        public void setMaxSize(int size) {
            this.maxSize = size;
        }
    }
}
```